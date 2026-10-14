package com.annalogue.m2.steps;

import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.*;
import io.cucumber.java.*;
import io.cucumber.java.fr.Et;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Paths;

public class RoleAcceptanceTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Et("{string} a le role {string}")
    public void loginALeRoleRole(String login, String role) {

        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Et("<login> a le role {string}")
    public void loginALeRole(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
