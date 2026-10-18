package com.annalogue.m2.steps;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class UnregisterUserAcceptanceTest {
    @Autowired
    IUserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    IInscriptionService inscriptionService;

    @Autowired
    IModuleService moduleService;

    // Shared between all tests in this class.
    static Playwright playwright;
    static Browser browser;
    static BrowserContext context;
    // New instance for each test method.

    Page page;

    @LocalServerPort
    private int port;

    private UserEntity user, membre;
    private ModuleEntity module, moia_fabrice;
    private static final String PASSWORD = "tin20hi7n@20ne";
    private  InscriptionEntity inscription;

    @BeforeAll
    public static void setupGlobal() {
        playwright = Playwright.create();
        // true for ci/cd
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
        context = browser.newContext(
                new Browser.NewContextOptions().setLocale("fr-FR")
        );

        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );

        page = context.newPage();

        inscriptionService.deleteAll();
        moduleService.deleteAll();
        userService.deleteAll();
    }

    @After
    public void teardownScenario() {
        try {
            inscriptionService.deleteAll();
            moduleService.deleteAll();
            userService.deleteAll();

        } finally {
            if (context != null) {
                context.tracing().stop(
                        new Tracing.StopOptions()
                                .setPath(Paths.get("trace.zip"))
                );


                context.close();
                context = null;
                page = null;
            }

        }
    }

    @Quand("je retire l'utilisateur {string} de la liste des inscrits du module {string}")
    public void retirer_utilisateur_de_la_liste_des_inscrits_du_module(String username, String name) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre = userService.findByEmail(username + "@example.com").orElseThrow(
                ()-> new  AssertionError("User not found : " + username+"@example.com")
        );

        assertNotNull(module);assertNotNull(membre);

        page.navigate("http://localhost:" + port + "/module-enrollments");

        Locator ligne = page.locator("tbody tr")
                .filter(new Locator.FilterOptions()
                        .setHas(page.getByText(
                                membre.getEmail(),
                                new Page.GetByTextOptions().setExact(true))))
                .filter(new Locator.FilterOptions()
                        .setHas(page.getByText(
                                module.getName(),
                                new Page.GetByTextOptions().setExact(true))));

        assertEquals(1, ligne.count());

        page.onDialog(Dialog::accept);

        ligne.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Delete")
        ).click();

        assertEquals(0, ligne.count());
    }

    @Alors("{string} ne figure plus dans la liste des membres du module {string}")
    public void ne_figure_plus_dans_la_liste_des_membres_du_module(String username, String name){
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre = userService.findByEmail(username + "@example.com").orElseThrow(
                ()-> new  AssertionError("User not found : " + username+"@example.com")
        );

        assertNotNull(module);assertNotNull(membre);


        boolean inscriptionExists = inscriptionService
                .findByModuleId(module.getId())
                .stream().anyMatch((inscription)-> inscription.getUser().getEmail().equals(membre.getEmail()));

        assertFalse(inscriptionExists);
        Locator ligne = page.locator("tbody tr")
                .filter(new Locator.FilterOptions()
                        .setHas(page.getByText(
                                membre.getEmail(),
                                new Page.GetByTextOptions().setExact(true))))
                .filter(new Locator.FilterOptions()
                        .setHas(page.getByText(
                                module.getName(),
                                new Page.GetByTextOptions().setExact(true))));
        assertEquals(0, ligne.count());
    }

    @Et("l'accès aux sujets du module {string} est refusé à {string}")
    public void accès_aux_sujets_du_module_est_refusé(String name, String username){
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre = userService.findByEmail(username + "@example.com").orElseThrow(
                ()-> new  AssertionError("User not found : " + username+"@example.com")
        );

        assertNotNull(module);assertNotNull(membre);


        BrowserContext userContext = browser.newContext();
        Page userPage = userContext.newPage();

        try {
            userPage.navigate("http://localhost:" + port + "/login");
            userPage.locator("#username").fill(membre.getEmail());
            userPage.locator("#password").fill(PASSWORD);
            userPage.locator("#login-btn").click();

            userPage.waitForURL("http://localhost:" + port + "/home");

            Response response = userPage.navigate(
                    "http://localhost:" + port + "/modules/" + module.getId());

            assertNotNull(response);
            assertEquals(403, response.status(),
                    "L'accès au module devrait être refusé");
        } finally {
            userContext.close();
        }
    }

}
