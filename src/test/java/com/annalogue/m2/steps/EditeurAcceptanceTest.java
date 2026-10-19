package com.annalogue.m2.steps;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CommentRepository;
import com.annalogue.m2.repositories.CorrectionRepository;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IPublicationService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitForSelectorState;
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

public class EditeurAcceptanceTest {
    @Autowired
    IUserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    IInscriptionService inscriptionService;

    @Autowired
    IModuleService moduleService;

    @Autowired
    private UserAcceptanceTest userSteps;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CorrectionRepository correctionRepository;

    @Autowired
    private IPublicationService publicationService;

    // the connection is done by UserAcceptanceTest: same browser page and same user
    private void syncWithLoggedUser() {
        page = userSteps.getPage();
        user = userSteps.getCurrentUser();
        assertNotNull(user, "No connected user: the step « Je suis connecté en tant que » must come first");
    }

    // Shared between all tests in this class.
    static Playwright playwright;
    static Browser browser;
    static BrowserContext context;
    // New instance for each test method.

    Page page;

    @LocalServerPort
    private int port;
    private InscriptionEntity inscription;
    private UserEntity user, membre;
    private ModuleEntity module, moia_fabrice;
    private static final String PASSWORD = "tin20hi7n@20ne";
    private String responseBody;

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

        commentRepository.deleteAllInBatch();
        correctionRepository.deleteAllInBatch();
        publicationService.deleteAll();
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

    @Soit("je suis authentifié en tant que {string} avec le rôle {string}")
    public void jeSuisAuthentifieAvecLeRole(
            String username, String role) {
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

    @Et("je suis responsable du module {string}")
    public void jeSuisResponsableDumodule(String name) {
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

    @Et("l'utilisateur {string} est inscrit au module {string} comme simple membre")
    public void utilisateurEstInscritCommeSimpleMembre(
            String username, String name) {
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
        inscriptionService.save(user, membre.getEmail(), module.getId());

        assertTrue(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getUsername().equals(username)
                        )
        );
    }

    @Quand("j'accorde le droit {string} à {string} pour le module {string}")
    public void jAccordeLeDroitPourLename(
            String droit, String username, String name) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre = userService.findByEmail(username + "@example.com").orElseThrow(
                () ->
                        new AssertionError("User not found : " + username)
        );

        inscription = inscriptionService.findByModuleId(module.getId())
                        .stream()
                                .filter(inscription1 -> inscription1.getUser().getEmail().equals(membre.getEmail()))
                                        .toList().getFirst();

        assertNotNull(inscription);
        assertFalse(inscription.isEditor());
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


        ligne.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Make editor")
        ).click();

    }

    @Alors("le statut de {string} sur le module {string} devient {string}")
    public void leStatutDeLEtudiantDevient(
            String username, String name, String statut) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre = userService.findByEmail(username + "@example.com").orElseThrow(
                () ->
                        new AssertionError("User not found : " + username)
        );

        inscription = inscriptionService.findByModuleId(module.getId())
                .stream()
                .filter(inscription1 -> inscription1.getUser().getEmail().equals(membre.getEmail()))
                .toList().getFirst();

        assertNotNull(inscription);

        assertTrue(inscription.isEditor());
    }

    @Et("j'ai reçu le droit {string} pour le module {string}")
    public void jaiRecuLeDroitPourLemodule(
            String droit, String name) {
        UserEntity fabrice= new UserEntity(
                "fabrice@example.com",
                passwordEncoder.encode(PASSWORD),
                "fabrice",
                Role.RESPONSABLE,
                0,
                "g"
        );
        userService.save(fabrice);

        assertTrue(userService.findByEmail(fabrice.getEmail()).isPresent());

        module = moduleService.createModule(name,fabrice);

        assertTrue(
                moduleService.existsByName(name)
        );

        assertNotNull(user);

        inscription = inscriptionService.save(fabrice,user.getEmail(), module.getId());
        inscriptionService.toggleEditor(inscription.getId());

        InscriptionEntity savedInscription = inscriptionService.findByModuleId(module.getId())
                .stream()
                .filter(inscription1 -> inscription1.getUser().getEmail().equals(user.getEmail()))
                .toList().getFirst();

        assertNotNull(savedInscription);
        assertTrue(savedInscription.isEditor());
    }

    @Quand("je consulte la page du module {string}")
    public void jeConsulteLaPageDumodule(String name) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );
        page.navigate("http://localhost:" + port + "/modules/" + module.getId());

    }


    @Alors("l'action {string} m'est accessible")
    public void lactionMestAccessible(String action) {
        assertNotNull(module);

        page.navigate("http://localhost:" + port + "/modules");

        Locator createButton = page.locator(
                "button[data-bs-target='#createModal']"
        );

        assertTrue(
                createButton.isVisible()
        );

        createButton.click();

        Locator modal = page.locator("#createModal");
        modal.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        Locator uploadBtn = modal.locator("a[href='/upload']");

        assertTrue(
                uploadBtn.isVisible()
        );

        uploadBtn.click();
        page.waitForURL("http://localhost:" + port + "/upload");

        Locator moduleSelect = page.locator("#module");

        Locator moduleOption = moduleSelect.locator(
                "option[value='" + module.getId() + "']"
        );

        assertEquals(
                1,
                moduleOption.count()
        );

        assertEquals(
                module.getName(),
                moduleOption.textContent().trim()
        );
    }


    @Et("un utilisateur {string} n'est pas inscrit au module {string}")
    public void utilisateurNestPasInscritAumodule(
            String username, String name) {
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

    @Quand("je tente d'accorder le droit {string} à {string} pour le module {string}")
    public void jeTenteDaccorderLeDroitPourLemodule(
            String droit, String username, String name) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );
        membre = userService.findByEmail(username + "@example.com").orElseThrow(
                ()->
                        new  AssertionError("Membre not found : " + username+"@example.com")
        );

        assertFalse(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getUsername().equals(username)
                        )
        );

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


        APIRequestContext request = page.context().request();

        RequestOptions options = RequestOptions.create()
                .setForm(FormData.create().set("_csrf", csrfToken))
                .setMaxRedirects(0);

        APIResponse response = request.post(
                "http://localhost:" + port
                        + "/module-enrollments/" + 999999L + "/toggle-editor",
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

    @Alors("l'action est impossible")
    public void lactionEstImpossible() {

        assertTrue(
                page.url().endsWith("/module-enrollments")
        );

        assertFalse(
                page.locator("#success").isVisible()
        );
    }

    @Et("un message indique que l'utilisateur doit d'abord être inscrit au module")
    public void messageIndiquantInscriptionPrealable() {
        assertTrue(
                page.url().endsWith("/module-enrollments")
        );

        assertTrue(
                page.locator("#error").isVisible()
        );

        assertEquals(
                "The user is not enrolled in this module.",
                page.locator("#error span").innerText().trim()
        );
    }

    @Et("{string} compte {string} et moi-même parmi ses membres")
    public void lemoduleCompteUnAutreusernameEtMoiMeme(
            String name, String username) {
        UserEntity fabrice = new UserEntity(
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


    @Quand("je tente d'attribuer le droit {string} à {string}")
    public void jeTenteDattribuerLeDroit(
            String droit, String username) {

        assertNotNull(moia_fabrice);

        membre = userService.findByEmail(username + "@example.com")
                .orElseThrow(() -> new AssertionError(
                        "Membre not found : " + username + "@example.com"
                ));

        inscription = inscriptionService.findByModuleId(moia_fabrice.getId())
                .stream()
                .filter(i -> i.getUser().getEmail().equals(membre.getEmail()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Inscription not found for " + username
                ));

        assertFalse(inscription.isEditor());
        page.navigate("http://localhost:" + port + "/module-enrollments");
        page.waitForURL("http://localhost:" + port + "/module-enrollments");

    }

    @Alors("l’action est refusée")
    public void lactionMestRefusee() {
        InscriptionEntity inscriptionAfter =
                inscriptionService.findByModuleId(moia_fabrice.getId())
                        .stream()
                        .filter(i -> i.getId().equals(inscription.getId()))
                        .findFirst()
                        .orElseThrow();

        assertFalse(inscriptionAfter.isEditor());
    }

    @Et("erreur d'autorisation est affiché")
    public void unMessageDerreurAutorisationEstAffiche() {
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

    @Et("je gère seulement le module {string}")
    public void jeGereSeulementLemodule(String name) {
        module = moduleService.createModule(name, user);

        assertNotNull(module);
        assertEquals(name, module.getName());
        assertEquals(user.getUsername(), module.getResponsible().getUsername());
        assertTrue(moduleService.existsByName(name));
    }

    @Et("{string} est géré par {string}")
    public void lemoduleEstGerePar(
            String name, String username) {
        UserEntity fabrice = new UserEntity(
                username+"@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.RESPONSABLE,
                0,
                "g"
        );

        userService.save(fabrice);

        moia_fabrice = moduleService.createModule(name, fabrice);

        assertTrue(moduleService.existsByName(name));
        assertTrue(moduleService.findByName(name).stream().anyMatch(m -> m.getResponsible().getEmail().equals(username+"@example.com")));
    }

    @Et("l'utilisateur {string} est inscrit à {string}")
    public void usernameEstInscritAumodule(
            String username, String name) {
        moia_fabrice = moduleService.findByName(name).orElseThrow(
                () ->  new AssertionError("Module not found")
        );
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
                moia_fabrice.getResponsible(),
                membre.getEmail(),
                moia_fabrice.getId()
        );

        assertTrue(inscriptionService.findByModuleId(moia_fabrice.getId()).stream().anyMatch(inscription1 -> inscription1.getUser().getEmail().equals(username + "@example.com")));

    }

    @Quand("je tente d'attribuer le droit {string} à {string} sur le module {string}")
    public void jeTenteDattribuerLeDroitSurLemodule(
            String droit, String username, String name) {
        assertNotNull(moia_fabrice);

        membre = userService.findByEmail(username + "@example.com")
                .orElseThrow(() -> new AssertionError(
                        "Membre not found : " + username + "@example.com"
                ));

        inscription = inscriptionService.findByModuleId(moia_fabrice.getId())
                .stream()
                .filter(i -> i.getUser().getEmail().equals(membre.getEmail()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Inscription not found for " + username
                ));

        assertFalse(inscription.isEditor());



        Locator csrfInput = page.locator("input[name='_csrf']");
        String csrfToken = null;

        if (csrfInput.count() > 0) {
            csrfToken = csrfInput.first().getAttribute("value");
        } else {
            Locator csrfMeta = page.locator("meta[name='_csrf']");

            if (csrfMeta.count() > 0) {
                csrfToken = csrfMeta.first().getAttribute("content");
            }
        }

        assertNotNull(csrfToken);

        APIRequestContext request = page.context().request();

        RequestOptions options = RequestOptions.create()
                .setForm(FormData.create().set("_csrf", csrfToken))
                .setMaxRedirects(0);

        APIResponse response = request.post(
                "http://localhost:" + port
                        + "/module-enrollments/" + inscription.getId()
                        + "/toggle-editor",
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

            assertFalse(inscription.isEditor());
        } finally {
            response.dispose();
        }
    }

    @Alors("la modification m'est refusée")
    public void laModificationMestRefusee() {
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


}
