package com.annalogue.m2.steps;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAcceptanceTest {

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

    @Quand("Je supprime <login> du site")
    public void jeSupprimeLoginDuSite() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Alors("J{string}erreur qui dit que l{string}existe pas")
    public void jAiUnMessageDErreurQuiDitQueLUtilisateurNExistePas() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Alors("J{string}erreur qui dit que je n'ai pas le droit de supprimer un utilisateur de site")
    public void jAiUnMessageDErreurQuiDitQueJeNAiPasLeDroitDeSupprimerUnUtilisateurDeSite() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Quand("J'ajoute un utilisateur {string} en tant que {string} avec le mot de passe <mdp> avec comme année {string}")
    public void jAjouteUnUtilisateurLoginEnTantQueRoleAvecLeMotDePasseMdpAvecCommeAnnée(String login,String role, String year) {
        page.navigate("http://localhost:8080/members/add");
        page.locator("input[name=\"email\"]").click();
        page.locator("input[name=\"email\"]").fill(login + "@example.com");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill(login);
        page.getByLabel("Role").selectOption(role);
        page.locator("#email").selectOption(year);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Member")).click();
    }

    @Alors("L'utilisateur <login> existe")
    public void lUtilisateurLoginExiste() {
        assertTrue(userService.findByEmail("toto@example.com").isPresent());
    }

    @Quand("J'ajoute un utilisateur {string} en tant que {string}")
    public void jAjouteUnUtilisateurEnTantQue(String login, String role) {
        page.navigate("http://localhost:8080/members/add");
        page.locator("input[name=\"email\"]").click();
        page.locator("input[name=\"email\"]").fill(login + "@example.com");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill(login);
        page.getByLabel("Role").selectOption(role);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Member")).click();
    }

    @Alors("J{string}erreur qui dit qu'on ne peut pas ajouter un autre administrateur")
    public void jAiUnMessageDErreurQuiDitQuOnNePeutPasAjouterUnAutreAdministrateur() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Quand("Je supprime {string} du site")
    public void jeSupprimeDuSite(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Et("<login> a le mot de passe <mdp>")
    public void loginALeMotDePasseMdp() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Et("L'utilisateur {string} n'existe pas")
    public void l_utilisateur_n_existe_pas() {
        assertFalse(userService.findByEmail("toto@example.com").isPresent());
    }

}
