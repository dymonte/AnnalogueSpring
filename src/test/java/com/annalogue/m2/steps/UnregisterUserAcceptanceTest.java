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
import com.microsoft.playwright.options.RequestOptions;
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
import com.microsoft.playwright.options.FormData;
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

    private UserEntity user, membre,fabrice;
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

    @Soit("En tant que {string} avec le rôle {string}")
    public void je_suis_connecte_avec_le_role(String username, String role) {
        Role r = switch (role.toLowerCase()) {
            case "administrateur" -> Role.ADMIN;
            case "responsable de module" -> Role.RESPONSABLE;
            case "membre" -> Role.PROF;
            default -> Role.ETUDIANT;
        };

        user = new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                r,
                0,
                "g"
        );

        userService.save(user);

        page.navigate("http://localhost:" + port + "/login");
        page.locator("#username").fill(username + "@example.com");
        page.locator("#password").fill(PASSWORD);
        page.locator("#login-btn").click();

        page.waitForURL("http://localhost:" + port + "/home");

        assertEquals(
                "http://localhost:" + port + "/home",
                page.url()
        );
    }

    @Et("je gère le module {string}")
    public void je_gere_le_module(String name) {
        module = moduleService.createModule(name,user);

        assertTrue(
                moduleService.existsByName(name)
        );

        ModuleEntity savedModule = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        assertEquals(name, savedModule.getName());
        assertEquals(user.getUsername(), savedModule.getResponsible().getUsername());
    }

    @Et("l'utilisateur {string} est inscrit au module {string}")
    public void utilisateur_est_inscrit_au_module(String username, String name) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name));

        membre = new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.PROF,
                0,
                "g"
        );

        userService.save(membre);

        inscription = inscriptionService.save(
                module.getResponsible(),
                membre.getEmail(),
                module.getId()
        );

        assertNotNull(inscription);

        assertTrue(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getEmail().equals(membre.getEmail()))
        );
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
        //
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
            assertEquals(403, response.status());
        } finally {
            userContext.close();
        }
    }

    @Et("l'utilisateur {string} est inscrit au module {string} avec le droit {string}")
    public void utilisateur_inscrit_au_module_droit_editeur(String username, String name, String editor){
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre =  new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.PROF,
                0,
                "g"
        );

        userService.save(membre);
        assertTrue(userService.findByEmail(username + "@example.com").isPresent());
        inscription = inscriptionService.save(
                user, membre.getEmail(), module.getId()
        );

        if ("Éditeur".equalsIgnoreCase(editor) && !inscription.isEditor()) {
            inscriptionService.toggleEditor(inscription.getId());
        }

        InscriptionEntity savedInscription =
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .filter(i -> i.getUser().getUsername().equals(username))
                        .findFirst()
                        .orElseThrow(() ->
                                new AssertionError(
                                        "Inscription not found for: " + username
                                ));

        assertNotNull(savedInscription);
        assertTrue(
                savedInscription.isEditor()
        );
    }

    @Quand("je désinscris {string} du module {string}")
    public void je_desinscris_du_module_du_module(String username, String name){
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
    }

    @Alors("{string} perd son statut d'éditeur sur le module {string}")
    public void utilisateur_perd_statut_editeur(String username, String name) {
        ModuleEntity moduleVerifie = moduleService.findByName(name)
                .orElseThrow(() -> new AssertionError("Module not found"));

        boolean inscriptionExiste = inscriptionService
                .findByModuleId(moduleVerifie.getId())
                .stream()
                .anyMatch(i ->
                        i.getUser().getEmail().equals(username + "@example.com"));

        assertFalse(inscriptionExiste);
    }

    @Et("{string} ne peut plus ajouter de nouveaux sujets dans le module {string}")
    public void utilisateur_ne_peut_plus_ajouter_de_sujets(
            String username, String name) {

        module = moduleService.findByName(name)
                .orElseThrow(() -> new AssertionError("Module not found"));

        membre = userService.findByEmail(username + "@example.com")
                .orElseThrow(() -> new AssertionError("user not found"));

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
            assertEquals(403, response.status());
        } finally {
            userContext.close();
        }
    }

    @Et("Je gère uniquement le module {string}")
    public void je_gere_uniquement_le_module(String name) {
        module = moduleService.createModule(name, user);

        assertNotNull(module);
        assertEquals(name, module.getName());
        assertEquals(user.getUsername(), module.getResponsible().getUsername());
        assertTrue(moduleService.existsByName(name));
    }

    @Et("Le module {string} est géré par {string}")
    public void le_module_est_gere_par(String name, String username) {
        fabrice = new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.RESPONSABLE,
                0,
                "g"
        );

        userService.save(fabrice);

        moia_fabrice = moduleService.createModule(name, fabrice);

        assertNotNull(moia_fabrice);
        assertEquals(name, moia_fabrice.getName());
        assertEquals(username, moia_fabrice.getResponsible().getUsername());

        assertNotEquals(module.getId(), moia_fabrice.getId());
    }

    @Quand("je tente de désinscrire {string} du module {string}")
    public void je_tente_de_desinscrire_du_module(String username, String name) {
        moia_fabrice = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name));

        membre = userService.findByEmail(username + "@example.com")
                .orElseThrow(() ->
                        new AssertionError("user not found : " + username));

        inscription = inscriptionService.findByModuleId(moia_fabrice.getId())
                .stream()
                .filter(i -> i.getUser().getEmail().equals(membre.getEmail()))
                .findFirst()
                .orElse(null);

        page.navigate("http://localhost:" + port + "/module-enrollments");

        Locator csrfInput = page.locator("input[name='_csrf']");
        String csrfToken;

        if (csrfInput.count() > 0) {
            csrfToken = csrfInput.first().getAttribute("value");
        } else {
            Locator csrfMeta = page.locator("meta[name='_csrf']");
            assertTrue(
                    csrfMeta.count() > 0
            );
            csrfToken = csrfMeta.getAttribute("content");
        }

        assertNotNull(csrfToken);

        long idASupprimer = (inscription == null)
                ? 999999L
                : inscription.getId();

        APIRequestContext request = page.context().request();

        RequestOptions options = RequestOptions.create()
                .setForm(FormData.create().set("_csrf", csrfToken))
                .setMaxRedirects(0);

        APIResponse response = request.post(
                "http://localhost:" + port
                        + "/module-enrollments/" + idASupprimer + "/delete",
                options
        );

        try {
            assertEquals(
                    302,
                    response.status()
            );

            page.navigate("http://localhost:" + port + "/module-enrollments");

            assertTrue(
                    page.url().endsWith("/module-enrollments")
            );

        } finally {
            response.dispose();
        }
    }

    @Alors("L'action m'est refusée")
    public void action_m_est_refusee() {
        assertTrue(
                page.url().endsWith("/module-enrollments")
        );

        assertTrue(
                page.locator("#error").isVisible()
        );

        assertEquals(
                "Enrollment not found or you are not authorized to modify or delete it.",
                page.locator("#error span").innerText().trim()
        );
    }

    @Et("{string} reste inscrit au module {string}")
    public void utilisateur_reste_inscrit_au_module(String username, String name) {
        ModuleEntity moduleVerifie = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name));

        boolean inscriptionExiste = inscriptionService
                .findByModuleId(moduleVerifie.getId())
                .stream()
                .anyMatch(i ->
                        i.getUser().getEmail().equals(username + "@example.com"));

        assertTrue(
                inscriptionExiste
        );
    }

    @Et("le module {string} compte {string} et moi-même parmi ses membres")
    public void le_module_moia_compte_fabien_et_moi_parmi_ses_memebres(String name, String username){
        fabrice = new UserEntity(
                "fabrice@example.com",
                passwordEncoder.encode(PASSWORD),
                "fabrice",
                Role.RESPONSABLE,
                0,
                "g"
        );

        userService.save(fabrice);

        moia_fabrice = moduleService.createModule(name, fabrice);

        membre = new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.PROF,
                0,
                "g"
        );

        userService.save(membre);

        assertTrue(
                userService.findByEmail(membre.getEmail()).isPresent()
        );


        inscriptionService.save(
                fabrice,
                membre.getEmail(),
                moia_fabrice.getId()
        );

        inscriptionService.save(
                fabrice,
                user.getEmail(),
                moia_fabrice.getId()
        );

        assertTrue(inscriptionService.findByModuleId(moia_fabrice.getId()).stream().anyMatch(inscription1 -> inscription1.getUser().getEmail().equals(username + "@example.com")));
        assertTrue(inscriptionService.findByModuleId(moia_fabrice.getId()).stream().anyMatch(inscription1 -> inscription1.getUser().getEmail().equals(user.getEmail())));

    }

    @Quand("je tente de retirer {string} du module {string}")
    public void je_tente_retirer_du_module(String username, String name) {
        moia_fabrice = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name));

        membre = userService.findByEmail(username + "@example.com")
                .orElseThrow(() ->
                        new AssertionError("user not found : " + username));

        inscription = inscriptionService.findByModuleId(moia_fabrice.getId())
                .stream()
                .filter(i ->
                        i.getUser().getEmail().equals(membre.getEmail()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                username + " not enrolled to the module " + name));

        page.navigate("http://localhost:" + port + "/module-enrollments");
        page.waitForURL("http://localhost:" + port + "/module-enrollments");
    }

    @Alors("l'option de désinscription ne m'est pas accessible")
    public void l_option_de_desinscription_ne_m_est_pas_accessible(){
        assertEquals("http://localhost:" + port + "/module-enrollments", page.url());
        assertEquals(
                Role.PROF,
                user.getRole()
        );

    }

    @Et("un message d'erreur d'autorisation est affiché")
    public void message_erreur_autorisation_affiche() {

        Locator errorCode = page.locator("#error-code");

        assertTrue(
                errorCode.isVisible()
        );

        assertEquals(
                "403",
                errorCode.innerText().trim()
        );

        Locator forbiddenMessage = page.locator("#forbidden-msg");

        assertTrue(
                forbiddenMessage.isVisible()
        );

        assertEquals(
                "You do not have permission to access this page.",
                forbiddenMessage.innerText().trim()
        );
    }

    @Et("l'utilisateur {string} n'est pas inscrit au module {string}")
    public void utilisateur_n_est_pas_inscrit_au_module(String username, String name) {
        membre = new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.PROF,
                0,
                "g"
        );

        userService.save(membre);
        UserEntity savedMembre = userService.findByEmail(username + "@example.com").orElseThrow(
                ()-> new AssertionError("Membre not found : " + username)
        );
        assertEquals(
                username,
                savedMembre.getUsername()
        );
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        assertFalse(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getUsername().equals(username)
                        )
        );
    }

    @Alors("un message d'erreur indique que l'utilisateur n'est pas inscrit au module")
    public void message_erreur_indique_utilisateur_non_inscrit() {

        assertNotNull(membre);
        assertNotNull(moia_fabrice);

        boolean inscriptionExiste = inscriptionService
                .findByModuleId(moia_fabrice.getId())
                .stream()
                .anyMatch(i ->
                        i.getUser().getEmail().equals(membre.getEmail()));

        assertFalse(
                inscriptionExiste
        );

        Locator erreur = page.locator("#error");
        erreur.waitFor();

        assertTrue(
                erreur.isVisible()
        );

        assertEquals(
                "The user is not enrolled in this module.",
                erreur.locator("span").innerText().trim()
        );
    }
}
