package com.annalogue.m2.steps;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.java.*;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Paths;
import java.util.Optional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationAcceptanceTest {

	@Autowired
	private static IUserService userService;

	@Autowired
	private static PasswordEncoder passwordEncoder;

    @Autowired
    private IModuleService moduleService;

	@LocalServerPort
	private int port;

	// shared between all tests
	static Playwright playwright;
	static Browser browser;
	static BrowserContext context;

	// new instance for each test
	Page page;

	private UserEntity admin;
	private String adminPassword = "admin";
	private UserEntity member;
	private String memberPassword = "member";
	private UserEntity respMod;
	private String respModPassword = "respMod";

	@BeforeAll
	public static void setupGlobal() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
	}

	@AfterAll
	public static void teardownGlobal() {
		if (browser != null) {
			browser.close();
		}
		if (playwright != null) {
			playwright.close();
		}
	}

	@Before
	public void setupScenario() {
		context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR")); // en-US
		context.tracing().start(new Tracing.StartOptions()
				.setScreenshots(true)
				.setSnapshots(true)
				.setSources(true));
		page = context.newPage();

        // delete all modules
        moduleService.deleteAll();

		// delete all users
		userService.deleteAll();

		// Create few test account
		admin = new UserEntity(
				"admin@example.com",
				passwordEncoder.encode(adminPassword),
				"admin",
				Role.ADMIN,
				0,
				"admins");

		member = new UserEntity(
				"member@example.com",
				passwordEncoder.encode(memberPassword),
				"memb", // In accordance with the acceptance test examples
				Role.ETUDIANT,
				0,
				"members");

		userService.save(admin);
		userService.save(member);

		// Go directly on website
		page.waitForURL("http://localhost:" + port + "/home");
		assertEquals("http://localhost:" + port + "/home", page.url());
	}

	@After
	public void teardownScenario() {
		if (context != null) {
			context.tracing().stop(new Tracing.StopOptions()
					.setPath(Paths.get("trace.zip")));
			context.close();
		}
		// delete all users
		userService.deleteAll();

        // delete all modules
        moduleService.deleteAll();
	}

	private Optional<UserEntity> getUserDependOnHisRole(String login) {
		return switch (login.toLowerCase()) {
			case "membre" -> Optional.of(member);
			case "administrateur" -> Optional.of(admin);
			case "responsable module" -> Optional.of(respMod);
			default -> Optional.empty();
		};
	}

	private String getPasswordDependOnUser(UserEntity user) {
		return switch (user.getRole()) {
			case Role.ADMIN -> adminPassword;
			case Role.ETUDIANT -> memberPassword;
			default -> adminPassword;
		};
	}

	@Soit("Je suis connecté en tant que {string}")
	public void jeSuisConnectéEnTantQue(String role) {

		UserEntity user = getUserDependOnHisRole(role)
				.orElseThrow(() -> new IllegalArgumentException("Rôle inconnu dans le Gherkin : " + role));

		assertTrue(userService.findByEmail(user.getEmail()).isPresent());

		// Connected user
		page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).click();
		page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill(user.getEmail());
		page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).press("Tab");
		page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(getPasswordDependOnUser(user));
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();

		assertThat(page.locator("#email")).matchesAriaSnapshot("- text: " + user.getEmail());
		throw new PendingException();
	}

}
