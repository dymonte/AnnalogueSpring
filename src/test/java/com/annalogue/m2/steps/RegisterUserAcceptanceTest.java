package com.annalogue.m2.steps;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.*;
import io.cucumber.java.*;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.cucumber.java.fr.Et;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterUserAcceptanceTest {

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

    @Et("l'utilisateur {string} est inscrit sur la plateforme mais non inscrit au module {string}")
    public void utilisateur_est_inscrit_mais_non_inscrit(String username, String name) {
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

    @Quand("j'inscris individuellement {string} au module {string}")
    public void j_inscris_individuellement_au_module(String username, String name) {
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        membre = userService.findByUsername(username).getFirst();
        assertNotNull(membre);

        page.navigate("http://localhost:" + port + "/module-enrollments/new");

        page.locator("#moduleId").selectOption(
                String.valueOf(module.getId())
        );

        page.locator("#emailUser").fill(username + "@example.com");
        page.locator("#enrolle-btn").click();

        page.waitForURL(
                "http://localhost:" + port + "/module-enrollments"
        );
        assertEquals("http://localhost:" + port + "/module-enrollments", page.url());
    }

    @Alors("{string} apparaît dans la liste des membres inscrits au module {string}")
    public void membre_apparait_dans_la_liste(String username, String name) {

        membre = userService.findByUsername(username).getFirst();
        assertNotNull(membre);
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        assertTrue(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getUsername().equals(username)
                        )
        );

        assertTrue(
                page.locator("#success").isVisible()
        );
        assertEquals("User successfully registered.", page.locator("#success span").innerText());
    }

    @Et("{string} a accès aux ressources du module {string}")
    public void membre_a_acces_aux_ressources(String username, String name) {
        membre = userService.findByUsername(username).getFirst();
        assertNotNull(membre);
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        assertTrue(
                inscriptionService.hasReaderAccess(membre, module)
        );
    }

    @Et("l'utilisateur {string} est déjà inscrit au module {string}")
    public void memere_deja_inscrit_au_module(String username, String name){


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

    @Quand("je tente d'inscrire à nouveau {string} au module {string}")
    public void je_tente_d_inscrire_a_nouveau_Fabien_au_module_MOIA(String username, String name){
        membre = userService.findByUsername(username).getFirst();
        assertNotNull(membre);
        module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );
        page.navigate(
                "http://localhost:" + port + "/module-enrollments/new"
        );

        page.locator("#moduleId")
                .selectOption(String.valueOf(module.getId()));

        page.locator("#emailUser").fill(membre.getEmail());
        page.locator("#enrolle-btn").click();


        page.waitForURL(
                "http://localhost:" + port + "/module-enrollments/new"
        );

        assertEquals("http://localhost:" + port + "/module-enrollments/new", page.url());

    }

    @Alors("l'inscription n'est pas dupliquée")
    public void inscription_non_dupliquee(){
        assertNotNull(membre);
        assertNotNull(module);

        long inscriptionsNbr = inscriptionService
                .findByModuleId(module.getId())
                .stream()
                .filter(i ->
                        i.getUser().getId().equals(membre.getId())
                )
                .count();

        assertEquals(
                1,
                inscriptionsNbr
        );
    }

    @Et("un message signale que {string} fait déjà partie des membres inscrits")
    public void message_signal_membre_deja_inscrit(String username) {
        assertEquals("http://localhost:" + port + "/module-enrollments/new", page.url());

        Locator message = page.locator("#userAlreadyRegisterError");
        assertTrue(message.isVisible());
        assertEquals("This user is already registered for this module.",message.innerText());
    }

    @Et("le module {string} compte {string} parmi ses membres")
    public void module_compte_parmi_ses_membres(String name, String username) {
        module = moduleService.createModule(name,user);

        assertTrue(
                moduleService.existsByName(name)
        );

        ModuleEntity savedModule = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        assertEquals(name, savedModule.getName());
        assertEquals(user.getUsername(), module.getResponsible().getUsername());

        membre =  new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.PROF,
                0,
                "g"
        );

        userService.save(membre);

        assertTrue(userService.findByEmail(membre.getEmail()).isPresent());

        inscriptionService.save(user, membre.getEmail(), module.getId());
        assertTrue(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getUsername().equals(username)
                        )
        );

    }

    @Quand("je tente d'inscrire un utilisateur au module {string}")
    public void je_tente_d_inscrire_a_nouveau_utilisateur_au_module_MOIA(String name){
       page.navigate(
                "http://localhost:" + port + "/module-enrollments/new"
        );
    }

    @Alors("l'action d'inscription ne m'est pas accessible")
    public void inscription_non_accessible(){
        //we should redirect to /error
        assertEquals("http://localhost:" + port + "/module-enrollments/new", page.url());
        assertEquals(
                Role.PROF,
                user.getRole()
        );
    }

    @Et("un message d'erreur d'autorisation est retourné")
    public void message_erreur_autorisation_retourne() {

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

    @Et("je gère uniquement le module {string}")
    public void je_gere_uniquement_le_module_TF(String name){
        module = moduleService.createModule(name,user);

        assertTrue(
                moduleService.existsByName(name)
        );
        user.setResponsibleModules(List.of(module));
        assertEquals(1, user.getResponsibleModules().size());
        assertEquals(name, user.getResponsibleModules().getFirst().getName());
    }

    @Et("le module {string} est géré par {string}")
    public void module_MOIA_gere_Fabrice(String name, String username){
        UserEntity fabrice= new UserEntity(
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                username,
                Role.RESPONSABLE,
                0,
                "g"
        );

        userService.save(fabrice);
        assertTrue(userService.findByEmail(fabrice.getEmail()).isPresent());

        moia_fabrice = moduleService.createModule(name,fabrice);

        assertTrue(
                moduleService.existsByName(name)
        );
        assertEquals(username,moia_fabrice.getResponsible().getUsername());
    }

    @Quand("je tente d'inscrire un membre au module {string}")
    public void je_tente_d_inscrire_un_membre_au_module(String name) {
        moia_fabrice = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        UserEntity newMembre = new UserEntity(
                "new_membre@example.com",
                passwordEncoder.encode(PASSWORD),
                "new_membre",
                Role.ETUDIANT,
                0,
                "g"
        );

        userService.save(newMembre);
        assertTrue(userService.findByEmail(newMembre.getEmail()).isPresent());
        page.navigate(
                "http://localhost:" + port + "/module-enrollments/new"
        );

        Locator moduleSelect = page.locator("#moduleId");

        assertTrue(moduleSelect.isVisible());

        assertEquals(
                0,
                moduleSelect.locator(
                        "option[value='" + moia_fabrice.getId() + "']"
                ).count()
        );
    }

    @Alors("l'action m'est refusée")
    public void action_est_refusee(){
        String url = page.url();
        assertTrue(url.equals("http://localhost:" + port + "/module-enrollments/new")
                || url.equals("http://localhost:" + port + "/modules/add"));

    }

    @Et("aucun membre n'est ajouté au module {string}")
    public void aucun_membre_n_est_ajoute_au_module(String name) {
        ModuleEntity module = moduleService.findByName(name)
                .orElseThrow(() ->
                        new AssertionError("Module not found : " + name)
                );

        assertFalse(
                inscriptionService.findByModuleId(module.getId())
                        .stream()
                        .anyMatch(i ->
                                i.getUser().getEmail()
                                        .equals("new_membre@example.com")
                        )
        );
    }

    @Et("il existe une cohorte nommée {string} regroupant plusieurs membres")
    public void cohorte_regroupant_plusieurs_membres(String nomCohorte) {
        // TODO :
    }

    @Quand("je sélectionne et inscris la cohorte {string} au module {string}")
    public void inscrire_cohorte_au_module(String nomCohorte, String nomModule) {
        // TODO :
    }

    @Alors("tous les membres de la cohorte {string} sont ajoutés au module {string}")
    public void tous_les_membres_sont_inscrits(String nomCohorte,String nomModule) {
        // TODO :
    }

    @Et("les étudiants de la cohorte {string} ont tous accès au contenu du module {string}")
    public void tous_les_etudiants_ont_acces(String nomCohorte, String nomModule) {
        // TODO :
    }

    public Page getPage() {
        return page;
    }

    public UserEntity getLoggedUser() {
        return user;
    }
}