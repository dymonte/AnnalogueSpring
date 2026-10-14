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


	@Soit("L'utilisateur \"ToTo\" possède un compte {string} ayant comme mot de passe \"titi\"")
	public void l_utilisateur_possede_un_compte_ayant_comme_mot_de_passe(String role) {
		Role r = switch (role.toLowerCase()) {
			case "membre" -> Role.ETUDIANT;
			case "administrateur" -> Role.ADMIN;
			case "responsable module" -> Role.PROF;
			default -> Role.ETUDIANT;
		};

		// create user
		UserEntity user = new UserEntity(
				"toto@example.com",
				passwordEncoder.encode("titi"),
				"ToTo",
				r,
				0,
				"g");
		userService.save(user);

		// check user informations in db
		assertTrue(userService.findByEmail("toto@example.com").isPresent());
		assertEquals(userService.findByEmail("toto@example.com").get().getRole(), r);
		assertTrue(
				passwordEncoder.matches("titi", userService.findByEmail("toto@example.com").get().getPasswordHash()));
		assertEquals("ToTo", userService.findByEmail("toto@example.com").get().getUsername());
	}

	@Quand("je m'authentifie avec l'identifiant \"ToTo\" et le mot de passe {string}")
	public void userLogsIn(String password) {
		// navigate to login page and fill form
		page.navigate("http://localhost:" + port + "/login");
		page.fill("#username", "toto@example.com");
		page.fill("#password", password);
		page.click("#login-btn");
	}

	@Alors("je suis connecté en tant que \"ToTo\" avec le rôle {string}")
	public void userIsLoggedInWithRole(String role) {
		page.waitForURL("http://localhost:" + port + "/home");

		// check url
		assertEquals("http://localhost:" + port + "/home", page.url());

		// check user informations
		String email = page.textContent("#email");
		assertNotNull(email);
		assertEquals("toto@example.com", email);

		// check role
		if (role.equals("Administrateur")) {
			assertEquals(200, page.navigate("http://localhost:" + port + "/admin").status());
		} else {
			assertEquals(403, page.navigate("http://localhost:" + port + "/admin").status());
		}

	}


	@Alors("j'ai un message d'erreur")
	public void j_ai_un_message_d_erreur() {
		// check url
		assertEquals("http://localhost:" + port + "/login?error", page.url());

		assertTrue(page.isVisible("#error"));
	}

	private UserEntity getUserDependOnHisLogin(String login) {
		return switch (login.toLowerCase()) {
			case "membre" -> member;
			case "administrateur" -> admin;
			case "responsable module" -> respMod;
			default -> admin;
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
	public void jeSuisConnectéEnTantQue(String login) {

		UserEntity user = getUserDependOnHisLogin(login);
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
