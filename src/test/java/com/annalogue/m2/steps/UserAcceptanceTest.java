package com.annalogue.m2.steps;

import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.services.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.Module;
import io.cucumber.java.*;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class UserAcceptanceTest {

    private static final String PASSWORD = "Passw0rd!";

    // id that no user has, to act on a user who does not exist
    private static final long UNKNOWN_ID = 999999L;

    @Autowired
    private IUserService userService;

    @Autowired
    private IModuleService moduleService;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private IPublicationService publicationService;

    @Autowired
    private IInscriptionService inscriptionService;

    @LocalServerPort
    private int port;

    // shared between all the scenarios
    static Playwright playwright;
    static Browser browser;

    // new for each scenario
    private BrowserContext context;
    private Page page;
    private boolean loggedAsAdmin;

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
        context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
        page = context.newPage();
        loggedAsAdmin = false;
    }

    @After
    public void teardownScenario() {
        if (context != null) {
            context.close();
        }
        moduleService.deleteAll();
        userService.deleteAll();
    }

    // ===== helpers =====

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String email(String login) {
        return login.toLowerCase() + "@example.com";
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }

    private UserEntity createUser(String emailAddress, String username, Role role) {
        return userService.save(
                new UserEntity(emailAddress, passwordEncoder.encode(PASSWORD), username, role, null, null));
    }

    private ModuleEntity createModule(String name) {
        return moduleRepository.save(new ModuleEntity(name,
                createUser("resp@example.com", "resp", Role.RESPONSABLE)));
    }

    private UserEntity findByEmailWithLogin(String login) {
        if (userService.findByEmail(email(login)).isPresent()) {
            return userService.findByEmail(email(login)).get();
        }

        return null;
    }

    // ===== Soit / Et =====

    @Soit("l'utilisateur {string} est inscrit au site")
    public void lUtilisateurEstInscritAuSite(String login) {
        if(!userService.existsByEmail(email(login))) {
            createUser(email(login), login, Role.ETUDIANT);
        };

        assertTrue(userService.existsByEmail(email(login)));
    }

    @Soit("L'utilisateur {string} possède un compte <role> ayant comme mot de passe {string}")
    public void lUtilisateurPossèdeUnCompteRoleAyantCommeMotDePasse(String login, String password) {
        if(!userService.existsByEmail(email(login))) {
            userService.save(new UserEntity(email(login), passwordEncoder.encode(password), login, Role.ETUDIANT, null, null));
        };

        assertTrue(userService.existsByEmail(email(login)));
    }

    @Et("{string} a accès au module {string}")
    public void aAccèsAuModule(String login, String module) {
        ModuleEntity moduleEntity = moduleService.findByName(module).orElseGet(() -> createModule(module));

        UserEntity student = findByEmailWithLogin(login);
        assertNotNull(student);

        UserEntity responsible = moduleEntity.getResponsible();

        boolean isEnrolled = inscriptionService.findInscriptions(responsible).stream()
                .anyMatch(ins -> ins.getUser().getId().equals(student.getId())
                        && ins.getModule().getId().equals(moduleEntity.getId()));

        if (!isEnrolled) {
            inscriptionService.save(responsible, email(login), moduleEntity.getId());
        }

        boolean hasAccess = inscriptionService.hasReaderAccess(student, moduleEntity);
        assertTrue(hasAccess);
    }

    @Et("le document {string} est disponible dans le module {string}")
    public void leDocumentEstDisponibleDansLeModule(String name, String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName).get();
        List<PublicationEntity> publications = moduleService.findAllPublicationsFromModule(module.getId());

        if (publicationService.findByName(name).isEmpty()) {
            publicationService.addPublication(
                    new PublicationEntity(name, name + "description", FileType.CM, module,
                            userService.findByEmail("resp@example.com").get(), "test/example/"));
        }

        assertTrue(publications.contains(publicationService.findByName(name).get()));
    }

    @Et("{string} n'a pas accès au module {string}")
    public void nAPasAccèsAuModule(String login, String module) {
        ModuleEntity moduleEntity = moduleService.findByName(module).orElseGet(() -> createModule(module));

        UserEntity student = findByEmailWithLogin(login);
        assertNotNull(student);

        inscriptionService.findByUserId(student.getId()).stream()
                .filter(ins -> ins.getModule().getId().equals(moduleEntity.getId()))
                .findFirst()
                .ifPresent(ins -> inscriptionService.deleteById(ins.getId()));

        boolean hasAccess = inscriptionService.hasReaderAccess(student, moduleEntity);
        assertFalse(hasAccess);
    }
    @Et("je recherche {string}")
    public void jeRecherche(String query) {
        page.locator("#search-bar").fill(query);
        page.click("#search-button");
    }

    @Et("aucun document accessible à {string} ne contient le mot {string}")
    public void aucunDocumentAccessibleÀNeContientLeMot(String login, String query) {
        UserEntity user = findByEmailWithLogin(login);
        assertNotNull(user);

        List<ModuleEntity> accessibleModules = moduleService.findAll().stream()
                .filter(module -> inscriptionService.hasReaderAccess(user, module))
                .toList();

        for (ModuleEntity module : accessibleModules) {
            List<PublicationEntity> publications = moduleService.findAllPublicationsFromModule(module.getId());
            for (PublicationEntity pub : publications) {
                boolean containsQuery = (pub.getName() != null && pub.getName().contains(query)) ||
                        (pub.getDescription() != null && pub.getDescription().contains(query));
                assertFalse(containsQuery);
            }
        }
    }

    // ==== Quand ====

    @Quand("je me connecte avec l'identifiant {string}")
    public void jeMeConnecteAvecLIdentifiant(String login) {
        String sessionEmail = email(login);
        if (userService.findByEmail(sessionEmail).isEmpty()) {
            createUser(sessionEmail, login, Role.ETUDIANT);
        }

        page.navigate(url("/login"));
        page.fill("#username", sessionEmail);
        page.fill("#password", PASSWORD);
        page.click("#login-btn");
        page.waitForURL(url("/home"));
    }

    @Quand("je m{string}identifiant {string} et le mot de passe {string}")
    public void jeMAuthentifieAvecLIdentifiantEtLeMotDePasse(String arg0, String arg1) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Quand("je m{string}identifiant {string}")
    public void jeMAuthentifieAvecLIdentifiant(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    // ===== Alors =====

    @Alors("le document {string} est affiché dans les résultats")
    public void leDocumentEstAffichéDansLesRésultats(String query) {
        assertThat(page.locator("#result-list").filter(new Locator.FilterOptions().setHasText(query))).isVisible();

    }

    @Alors("le document {string} n'est pas affiché dans les résultats")
    public void leDocumentNEstPasAffichéDansLesRésultats(String query) {
        assertThat(page.locator("#result-list").filter(new Locator.FilterOptions().setHasText(query))).not().isVisible();
    }

    @Alors("aucun document n'est affiché dans les résultats")
    public void aucunDocumentNEstAffichéDansLesRésultats() {
        assertTrue(page.locator("#result-list").count() == 0);
    }

    @Alors("je suis connecté en tant que {string} avec le rôle <role>")
    public void jeSuisConnectéEnTantQueAvecLeRôleRole(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Alors("j{string}erreur")
    public void jAiUnMessageDErreur() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
