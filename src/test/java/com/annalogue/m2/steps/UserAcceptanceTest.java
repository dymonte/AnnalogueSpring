package com.annalogue.m2.steps;

import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.CommentRepository;
import com.annalogue.m2.repositories.CorrectionRepository;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.services.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.FilePayload;
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserAcceptanceTest {

    private static final String PASSWORD = "Passw0rd!";

    // id that no user has, to act on a user who does not exist
    private static final long UNKNOWN_ID = 999999L;

    // minimal content of a pdf, enough for the upload
    private static final byte[] PDF_CONTENT = "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.UTF_8);

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

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private CorrectionRepository correctionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @LocalServerPort
    private int port;

    private CorrectionEntity currentCorrection;

    // shared between all the scenarios
    static Playwright playwright;
    static Browser browser;

    // new for each scenario
    private BrowserContext context;
    private Page page;
    private boolean loggedAsAdmin;

    // state of the correction scenarios
    private UserEntity currentUser;
    private PublicationEntity currentSubject;
    private String currentCorrectionTitle;
    // true once the user has started to act on the page (Quand): before, the steps only prepare the data
    private boolean correctionSubmitted;

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
        currentUser = null;
        currentSubject = null;
        currentCorrectionTitle = null;
        correctionSubmitted = false;
        currentCorrection = null;
    }

    @After
    public void teardownScenario() {
        if (context != null) {
            context.close();
        }
        commentRepository.deleteAllInBatch();
        correctionRepository.deleteAllInBatch();
        publicationService.deleteAll();
        inscriptionService.deleteAll();
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

    // role written in the Gherkin -> role of the application
    // « Membre » is a teacher, like in AdminMembersAcceptanceTest
    private Role role(String gherkinRole) {
        return switch (gherkinRole.trim().toLowerCase()) {
            case "administrateur" -> Role.ADMIN;
            case "responsable module", "responsable de module" -> Role.RESPONSABLE;
            case "membre" -> Role.PROF;
            default -> throw new IllegalArgumentException("Unknown role in the Gherkin: " + gherkinRole);
        };
    }

    // role in a module written in the Gherkin -> type of the inscription
    private InscriptionType moduleRole(String gherkinModuleRole) {
        return switch (gherkinModuleRole.trim().toLowerCase()) {
            case "intervenant" -> InscriptionType.INTERVENANT;
            case "etudiant", "étudiant" -> InscriptionType.ETUDIANT;
            default -> throw new IllegalArgumentException("Unknown module role in the Gherkin: " + gherkinModuleRole);
        };
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


    // fills the login form and waits for the result: /home if accepted, /login?error if refused
    private void login(String login, String password) {
        page.navigate(url("/login"));
        page.fill("#username", email(login));
        page.fill("#password", password);
        page.click("#login-btn");
        page.waitForURL(u -> u.contains("/home") || u.contains("/login?error"));
    }

    // http status of a page, to check what the connected user is allowed to open
    private int statusOf(String path) {
        return page.navigate(url(path)).status();
    }

    // the module with its responsible (another user than the connected one), created if it does not exist
    private ModuleEntity getOrCreateModule(String name) {
        return moduleService.findByName(name).orElseGet(() -> {
            UserEntity responsible = userService.findByEmail("resp@example.com")
                    .orElseGet(() -> createUser("resp@example.com", "resp", Role.RESPONSABLE));
            return moduleService.createModule(name, responsible);
        });
    }

    private Optional<InscriptionEntity> findInscription(UserEntity user, ModuleEntity module) {
        return inscriptionService.findByUserId(user.getId()).stream()
                .filter(ins -> ins.getModule().getId().equals(module.getId()))
                .findFirst();
    }

    private PublicationEntity findSubject(String subjectName) {
        return publicationService.findByName(subjectName)
                .orElseThrow(() -> new AssertionError("Subject not found : " + subjectName));
    }

    private Optional<CorrectionEntity> findCorrection(String title, PublicationEntity subject) {
        for (CorrectionEntity correction : correctionRepository.findAll()) {
            if (title.equals(correction.getTitle())
                    && correction.getPublication() != null
                    && correction.getPublication().getId().equals(subject.getId())) {
                return Optional.of(correction);
            }
        }
        return Optional.empty();
    }

    private String modulePageUrl(ModuleEntity module) {
        return url("/modules/" + module.getId());
    }

    private String detailsPageUrl(PublicationEntity subject) {
        return url("/publications/" + subject.getId() + "/details");
    }

    // form of the details page of a subject (the navbar has other forms: the action identifies it)
    private String subjectCommentForm(PublicationEntity subject) {
        return "form[action$='/publications/" + subject.getId() + "/add-comment']";
    }

    private String correctionCommentForm(CorrectionEntity correction) {
        return "form[action$='/modules/" + correction.getId() + "/add-comment-correction']";
    }

    // comments of a subject (a comment of a correction has no publication)
    private List<CommentEntity> subjectComments(PublicationEntity subject) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getPublication() != null && c.getPublication().getId().equals(subject.getId()))
                .toList();
    }

    private List<CommentEntity> correctionComments(CorrectionEntity correction) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getCorrection() != null && c.getCorrection().getId().equals(correction.getId()))
                .toList();
    }

    private boolean hasComment(List<CommentEntity> comments, String login, String content) {
        return comments.stream().anyMatch(c -> content.equals(c.getContent())
                && c.getAuthorComment() != null
                && email(login).equals(c.getAuthorComment().getEmail()));
    }

    // card of a correction on the module page (the delete form identifies it): its comments are the articles inside
    private Locator correctionCard(CorrectionEntity correction) {
        return page.locator("div.card-body.p-3").filter(new Locator.FilterOptions()
                .setHas(page.locator("form[action$='/deleteCorrection/" + correction.getId() + "']")));
    }

    // ===== Soit / Et =====

    @Soit("l'utilisateur {string} est inscrit au site")
    public void lUtilisateurEstInscritAuSite(String login) {
        if(!userService.existsByEmail(email(login))) {
            createUser(email(login), login, Role.ETUDIANT);
        };

        assertTrue(userService.existsByEmail(email(login)));
    }

    @Soit("L'utilisateur {string} possède un compte {string} ayant comme mot de passe {string}")
    public void lUtilisateurPossèdeUnCompteAyantCommeMotDePasse(String login, String gherkinRole, String password) {
        if (!userService.existsByEmail(email(login))) {
            userService.save(new UserEntity(email(login), passwordEncoder.encode(password), login,
                    role(gherkinRole), null, null));
        }

        assertTrue(userService.existsByEmail(email(login)));
    }

    @Soit("Je suis connecté en tant que {string} avec le rôle {string}")
    public void jeSuisConnecteEnTantQueAvecLeRole(String login, String gherkinRole) {

        Role role = role(gherkinRole);

        currentUser = userService.findByEmail(email(login))
                .orElseGet(() -> createUser(email(login), login, role));

        System.out.println("Utilisateur : " + email(login));
        System.out.println("Utilisateur trouvé : " + (currentUser != null));
        System.out.println("Mot de passe utilisé : " + PASSWORD);

        System.out.println("Email : " + currentUser.getEmail());

        System.out.println("Hash présent : "
                + (currentUser.getPasswordHash() != null));

        System.out.println("Mot de passe correspondant : "
                + passwordEncoder.matches(PASSWORD, currentUser.getPasswordHash()));

        assertTrue(userService.existsByEmail(email(login)));

        page.navigate(url("/login"));
        page.fill("#username", email(login));
        page.fill("#password", PASSWORD);
        page.click("#login-btn");
        try {
            page.waitForURL(url("/home"));
        } catch (PlaywrightException e) {
            String error = page.locator("#error").count() > 0
                    ? page.locator("#error").innerText()
                    : "(no #error)";

            fail("Login of " + email(login) + " failed. Url: " + page.url()
                    + " - message: " + error
                    + " - page: " + page.locator("body").innerText());
        }

        loggedAsAdmin = role == Role.ADMIN;
    }

    @Et("J'ai créé le module {string}")
    public void jAiCrééLeModule(String moduleName) {
        assertNotNull(currentUser);

        ModuleEntity module = moduleService.createModule(moduleName, currentUser);

        assertTrue(moduleService.existsByName(moduleName));
        assertEquals(currentUser.getId(), module.getResponsible().getId());
    }

    @Et("Je suis inscrit au module {string}")
    public void jeSuisInscritAuModule(String moduleName) {
        assertNotNull(currentUser);

        ModuleEntity module = getOrCreateModule(moduleName);

        if (findInscription(currentUser, module).isEmpty()) {
            inscriptionService.save(module.getResponsible(), currentUser.getEmail(), module.getId());
        }

        assertTrue(findInscription(currentUser, module).isPresent());
    }

    @Et("J'ai le role {string} pour le module {string}")
    public void jAiLeRolePourLeModule(String gherkinModuleRole, String moduleName) {
        assertNotNull(currentUser);

        InscriptionType expectedType = moduleRole(gherkinModuleRole);
        ModuleEntity module = moduleService.findByName(moduleName)
                .orElseThrow(() -> new AssertionError("Module not found : " + moduleName));

        InscriptionEntity inscription = findInscription(currentUser, module)
                .orElseThrow(() -> new AssertionError("Not enrolled in the module : " + moduleName));

        // the service gives the type from the role of the user: here the type is forced
        inscription.setInscriptionType(expectedType);
        inscriptionRepository.save(inscription);

        assertEquals(expectedType, findInscription(currentUser, module).orElseThrow().getInscriptionType());
    }

    @Et("Je suis sur la page du module {string}")
    public void jeSuisSurLaPageDuModule(String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName)
                .orElseThrow(() -> new AssertionError("Module not found : " + moduleName));

        page.navigate(modulePageUrl(module));

        assertThat(page).hasURL(modulePageUrl(module));
    }

    @Et("Le sujet {string} existe sur le module {string}")
    public void leSujetExisteSurLeModule(String subjectName, String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName)
                .orElseThrow(() -> new AssertionError("Module not found : " + moduleName));

        if (publicationService.findByName(subjectName).isEmpty()) {
            publicationService.addPublication(
                    new PublicationEntity(subjectName, subjectName + "description", FileType.TD, module,
                            module.getResponsible(), "test/example/" + subjectName + ".pdf"));
        }

        currentSubject = findSubject(subjectName);
        assertEquals(module.getId(), currentSubject.getModule().getId());

        // the subject was created after the page was displayed
        if (page.url().equals(modulePageUrl(module))) {
            page.reload();
        }
    }

    @Et("La correction {string} est disponible pour le sujet {string}")
    public void laCorrectionEstDisponiblePourLeSujet(String title, String subjectName) {
        PublicationEntity subject = findSubject(subjectName);
        String moduleUrl = modulePageUrl(subject.getModule());

        if (!correctionSubmitted) {
            if (findCorrection(title, subject).isEmpty()) {
                UserEntity author = (currentUser != null)
                        ? currentUser
                        : subject.getModule().getResponsible();

                correctionRepository.save(new CorrectionEntity(
                        author, subject, title, "test/example/" + title + ".pdf"));
            }

            if (page.url().equals(moduleUrl)) {
                page.reload();
            }
        }

        assertTrue(findCorrection(title, subject).isPresent());

        if (correctionSubmitted) {
            page.navigate(moduleUrl);
            assertThat(page.getByText(title,
                    new Page.GetByTextOptions().setExact(true))).isVisible();
        }
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

        if (publicationService.findByName(name).isEmpty()) {
            publicationService.addPublication(
                    new PublicationEntity(name, name + "description", FileType.CM, module,
                            userService.findByEmail("resp@example.com").get(), "test/example/"));
        }

        PublicationEntity publication = publicationService.findByName(name).orElseThrow();

        boolean inModule = publicationService.findByModule(module).stream()
                .anyMatch(p -> p.getId().equals(publication.getId()));

        assertTrue(inModule);
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

    @Quand("je m'authentifie avec l'identifiant {string} et le mot de passe {string}")
    public void jeMAuthentifieAvecLIdentifiantEtLeMotDePasse(String login, String password) {
        login(login, password);
    }

    // the password field is required in the form: any password is sent, the account does not exist
    @Quand("je m'authentifie avec l'identifiant {string}")
    public void jeMAuthentifieAvecLIdentifiant(String login) {
        login(login, PASSWORD);
    }

    @Quand("J'ajoute une correction {string} au sujet {string}")
    public void jAjouteUneCorrectionAuSujet(String title, String subjectName) {
        correctionSubmitted = true;
        currentSubject = findSubject(subjectName);
        currentCorrectionTitle = title;
        long id = currentSubject.getId();

        // opens the form « Add a correction » of the subject, then types the title
        page.click("[data-bs-target='#correction-form-" + id + "']");
        assertThat(page.locator("#correction-title-" + id)).isVisible();
        page.fill("#correction-title-" + id, title);
    }

    @Quand("Je télécharge le fichier {string} sur la page")
    public void jeTéléchargeLeFichierSurLaPage(String fileName) {
        assertNotNull(currentSubject);

        page.locator("#correction-file-" + currentSubject.getId())
                .setInputFiles(new FilePayload(fileName, "application/pdf", PDF_CONTENT));
    }

    @Quand("Je valide l'ajout de la correction")
    public void jeValideLAjoutDeLaCorrection() {
        assertNotNull(currentSubject);

        String submitButton =
                "#correction-form-" + currentSubject.getId()
                        + " button[type='submit']";

        // submits the form and waits for the redirect to the module page
        page.waitForNavigation(() -> page.click(submitButton));

        assertThat(page).hasURL(modulePageUrl(currentSubject.getModule()));
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

    @Alors("je suis connecté en tant que {string} avec le rôle {string}")
    public void jeSuisConnectéEnTantQueAvecLeRôle(String login, String gherkinRole) {
        Role expected = role(gherkinRole);

        // the user is logged in: redirected to /home, his name is in the navbar
        assertTrue(page.url().endsWith("/home"));
        assertThat(page.locator("#dropdownLogout"))
                .containsText(Pattern.compile(login, Pattern.CASE_INSENSITIVE));

        // the role is the one of the account: what he can open depends on it (SecurityConfig)
        assertEquals(expected == Role.ADMIN ? 200 : 403, statusOf("/admin"));
        assertEquals(expected == Role.RESPONSABLE ? 200 : 403, statusOf("/modules/add"));
    }

    @Alors("j'ai un message d'erreur")
    public void jAiUnMessageDErreur() {
        assertTrue(page.url().contains("/login"));
        assertThat(page.locator("#error")).containsText(message("error.login.generic"));
    }

    @Alors("Le fichier {string} est associé à la correction {string}")
    public void leFichierEstAssociéÀLaCorrection(String fileName, String title) {

        assertNotNull(currentSubject);

        CorrectionEntity correction = findCorrection(title, currentSubject)
                .orElseThrow(() -> new AssertionError("Correction not found : " + title));

        // the storage adds a number when the name is already used: correction1.pdf -> correction1-1.pdf
        String storedName = Path.of(correction.getPath()).getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String base = dot < 0 ? fileName : fileName.substring(0, dot);
        String extension = dot < 0 ? "" : fileName.substring(dot);

        assertTrue(storedName.matches(Pattern.quote(base) + "(-\\d+)?" + Pattern.quote(extension)),
                "Unexpected stored file : " + storedName);
        assertTrue(new File(correction.getPath()).exists(), "File not stored : " + correction.getPath());
    }

    @Quand("J'ajoute un commentaire au sujet {string}")
    public void jAjouteUnCommentaireAuSujet(String subjectName) {
        currentSubject = findSubject(subjectName);
        currentCorrection = null;

        // the comments of a subject are on its details page, reached from the module page
        page.click("a[href$='/publications/" + currentSubject.getId() + "/details']");
        page.waitForURL(detailsPageUrl(currentSubject));
        assertThat(page.locator(subjectCommentForm(currentSubject))).isVisible();
    }

    @Quand("J'ajoute un commentaire à la correction {string} du sujet {string}")
    public void jAjouteUnCommentaireÀLaCorrectionDuSujet(String title, String subjectName) {
        currentSubject = findSubject(subjectName);
        currentCorrection = findCorrection(title, currentSubject)
                .orElseThrow(() -> new AssertionError("Correction not found : " + title));
        long id = currentCorrection.getId();

        // opens the form « Add a comment » of the correction, on the module page
        page.click("[data-bs-target='#comment-form-" + id + "']");

        // waits for the end of the animation: during it (class « collapsing »), Bootstrap ignores a new click
        assertThat(page.locator("#comment-form-" + id)).hasClass(Pattern.compile("\\bshow\\b"));
        assertThat(page.locator("#content-" + id)).isVisible();
    }

    @Et("Je saisis le commentaire {string}")
    public void jeSaisisLeCommentaire(String content) {
        assertNotNull(currentSubject);

        if (currentCorrection != null) {
            page.fill("#content-" + currentCorrection.getId(), content);
        } else {
            page.fill(subjectCommentForm(currentSubject) + " textarea[name='content']", content);
        }
    }

    @Et("Je valide l'ajout du commentaire")
    public void jeValideLAjoutDuCommentaire() {
        assertNotNull(currentSubject);

        String form = currentCorrection != null
                ? correctionCommentForm(currentCorrection)
                : subjectCommentForm(currentSubject);

        // submits the form and waits for the redirect to the module page
        page.waitForNavigation(() -> page.click(form + " button[type='submit']"));

        assertThat(page).hasURL(modulePageUrl(currentSubject.getModule()));
    }

    // no cancel button in the pages: the user leaves the form without submitting it
    @Et("J'annule l'ajout du commentaire")
    public void jAnnuleLAjoutDuCommentaire() {
        assertNotNull(currentSubject);

        if (currentCorrection != null) {
            long id = currentCorrection.getId();
            // closes the form of the correction
            page.click("[data-bs-target='#comment-form-" + id + "']");
            assertThat(page.locator("#content-" + id)).isHidden();
        } else {
            // « go back » button of the details page
            page.click("a[aria-label='Go back']");
            page.waitForURL(modulePageUrl(currentSubject.getModule()));
        }
    }

    @Et("Le sujet {string} du module {string} dispose de la correction {string}")
    public void leSujetDuModuleDisposeDeLaCorrection(String subjectName, String moduleName, String title) {
        ModuleEntity module = moduleService.findByName(moduleName)
                .orElseThrow(() -> new AssertionError("Module not found : " + moduleName));
        PublicationEntity subject = findSubject(subjectName);
        assertEquals(module.getId(), subject.getModule().getId());

        if (findCorrection(title, subject).isEmpty()) {
            UserEntity author = (currentUser != null) ? currentUser : module.getResponsible();
            correctionRepository.save(new CorrectionEntity(
                    author, subject, title, "test/example/" + title + ".pdf"));
        }
        assertTrue(findCorrection(title, subject).isPresent());

        // the correction was created after the page was displayed
        if (page.url().equals(modulePageUrl(module))) {
            page.reload();
        }
    }

    @Alors("Un commentaire de l'utilisateur {string} disant {string} est disponible pour le sujet {string} sur le module {string}")
    public void unCommentaireDeLUtilisateurDisantEstDisponiblePourLeSujetSurLeModule(
            String login, String content, String subjectName, String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName)
                .orElseThrow(() -> new AssertionError("Module not found : " + moduleName));
        PublicationEntity subject = findSubject(subjectName);
        assertEquals(module.getId(), subject.getModule().getId());

        assertTrue(hasComment(subjectComments(subject), login, content),
                "Comment not stored for the subject : " + subjectName);

        // displayed on the details page of the subject, with the email of the author
        page.navigate(detailsPageUrl(subject));
        assertThat(page.locator("article")
                .filter(new Locator.FilterOptions().setHasText(content))
                .filter(new Locator.FilterOptions().setHasText(email(login)))
                .first()).isVisible();
    }

    @Alors("Un commentaire de l'utilisateur {string} disant {string} est disponible pour la correction {string} du sujet {string} sur le module {string}")
    public void unCommentaireDeLUtilisateurDisantEstDisponiblePourLaCorrectionDuSujetSurLeModule(
            String login, String content, String title, String subjectName, String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName)
                .orElseThrow(() -> new AssertionError("Module not found : " + moduleName));
        PublicationEntity subject = findSubject(subjectName);
        assertEquals(module.getId(), subject.getModule().getId());
        CorrectionEntity correction = findCorrection(title, subject)
                .orElseThrow(() -> new AssertionError("Correction not found : " + title));

        assertTrue(hasComment(correctionComments(correction), login, content),
                "Comment not stored for the correction : " + title);

        // displayed under the correction, on the module page
        page.navigate(modulePageUrl(module));
        assertThat(correctionCard(correction).locator("article")
                .filter(new Locator.FilterOptions().setHasText(content))
                .filter(new Locator.FilterOptions().setHasText(email(login)))
                .first()).isVisible();
    }

    @Alors("Aucun commentaire n'est disponible pour le sujet {string}")
    public void aucunCommentaireNEstDisponiblePourLeSujet(String subjectName) {
        PublicationEntity subject = findSubject(subjectName);

        assertTrue(subjectComments(subject).isEmpty(), "A comment exists for the subject : " + subjectName);

        // the comments are the only articles of the details page
        page.navigate(detailsPageUrl(subject));
        assertThat(page.locator("article")).hasCount(0);
    }

    @Alors("Aucun commentaire n'est disponible pour la correction {string} du sujet {string}")
    public void aucunCommentaireNEstDisponiblePourLaCorrectionDuSujet(String title, String subjectName) {
        PublicationEntity subject = findSubject(subjectName);
        CorrectionEntity correction = findCorrection(title, subject)
                .orElseThrow(() -> new AssertionError("Correction not found : " + title));

        assertTrue(correctionComments(correction).isEmpty(), "A comment exists for the correction : " + title);

        page.navigate(modulePageUrl(subject.getModule()));
        assertThat(correctionCard(correction).locator("article")).hasCount(0);
    }


}