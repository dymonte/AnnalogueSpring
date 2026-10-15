package com.annalogue.m2.steps;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import io.cucumber.java.*;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// ADMIN-07: 817_Supression_utilisateur.feature
// ADMIN-02: 820_Gestion_des_roles.feature
public class AdminMembersAcceptanceTest {

    private static final String PASSWORD = "Passw0rd!";
    private static final String WRONG_PASSWORD = "wrong!";
    // id that no user has, to act on a user who does not exist
    private static final long UNKNOWN_ID = 999999L;

    @Autowired
    private IUserService userService;

    @Autowired
    private IModuleService moduleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

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
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
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

    // role written in the Gherkin -> role of the application
    // « Membre » is a teacher here: only a teacher can become « Responsable Module »
    private Role role(String gherkinRole) {
        return switch (gherkinRole.trim().toLowerCase()) {
            case "administrateur" -> Role.ADMIN;
            case "responsable module", "responsable de module" -> Role.RESPONSABLE;
            case "membre" -> Role.PROF;
            default -> throw new IllegalArgumentException("Unknown role in the Gherkin: " + gherkinRole);
        };
    }

    // value sent by the form: the enum name, or an unknown value for a role that does not exist
    private String roleValue(String gherkinRole) {
        return switch (gherkinRole.trim().toLowerCase()) {
            case "administrateur" -> Role.ADMIN.name();
            case "responsable module", "responsable de module" -> Role.RESPONSABLE.name();
            case "membre" -> Role.PROF.name();
            default -> gherkinRole.trim().toUpperCase();
        };
    }

    private Optional<UserEntity> user(String login) {
        return userService.findByEmail(email(login));
    }

    private UserEntity createUser(String emailAddress, String username, Role role) {
        return userService.save(
                new UserEntity(emailAddress, passwordEncoder.encode(PASSWORD), username, role, null, null));
    }

    private String row(String login) {
        return "tr[data-email='" + email(login) + "']";
    }

    private String visibleText() {
        return page.locator("body").innerText();
    }

    // posts a form like the browser does (with the CSRF token), for the cases the page does not offer
    private void postForm(String path, Map<String, String> fields) {
        if (!page.url().startsWith(url("/"))) {
            page.navigate(url("/home"));
        }
        Map<String, String> allFields = new LinkedHashMap<>(fields);
        allFields.put("_csrf", page.getAttribute("input[name='_csrf']", "value"));

        page.waitForNavigation(() -> page.evaluate("""
                ([action, fields]) => {
                    const form = document.createElement('form');
                    form.method = 'post';
                    form.action = action;
                    for (const [name, value] of Object.entries(fields)) {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = name;
                        input.value = value;
                        form.appendChild(input);
                    }
                    document.body.appendChild(form);
                    form.submit();
                }""", List.of(url(path), allFields)));
    }

    private long idOf(String login) {
        return user(login).map(UserEntity::getId).orElse(UNKNOWN_ID);
    }

    private void addUser(String login, String gherkinRole, String year) {
        page.navigate(url("/members/add"));

        if (page.locator("input[name=\"email\"]").count() == 0) {
            return;
        }

        page.fill("input[name=\"email\"]", email(login));
        page.fill("#username", login);

        String value = roleValue(gherkinRole);
        Locator roleSelect = page.locator("#role");
        if (roleSelect.locator("option[value='" + value + "']").count() == 0) {
            roleSelect.evaluate("(select, v) => select.add(new Option(v, v))", value);
        }

        roleSelect.selectOption(value);

        Locator yearSelect = page.locator("#year");
        if (year != null && yearSelect.isEnabled()) {
            yearSelect.selectOption(new SelectOption().setLabel(year));
        }

        page.waitForNavigation(() ->
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Member")).click());
    }

    // ===== Soit / Et =====

    @Soit("Je suis connecté en tant que {string}")
    public void jeSuisConnecteEnTantQue(String gherkinRole) {
        Role role = role(gherkinRole);
        String sessionEmail = "session-" + role.name().toLowerCase() + "@example.com";
        if (userService.findByEmail(sessionEmail).isEmpty()) {
            createUser(sessionEmail, "session-" + role.name().toLowerCase(), role);
        }

        page.navigate(url("/login"));
        page.fill("#username", sessionEmail);
        page.fill("#password", PASSWORD);
        page.click("#login-btn");
        page.waitForURL(url("/home"));

        loggedAsAdmin = role == Role.ADMIN;
    }

    @Soit("Je suis sur la page de connexion")
    public void jeSuisSurLaPageDeConnexion() {
        page.navigate(url("/login"));
        assertThat(page.locator("#username")).isEnabled();
        assertThat(page.locator("#password")).isEnabled();
        assertThat(page.locator("#login-btn")).isEnabled();
    }

    // quotes are optional: the examples of 820 write ERM and Membre without quotes.
    // before an action it creates the user; after an action it checks his role
    @Et("^\"?([^\" ]+)\"? a le role \"?([^\"]+?)\"?$")
    public void aLeRole(String login, String gherkinRole) {
        Role expected = role(gherkinRole);
        UserEntity user = user(login).orElseGet(() -> createUser(email(login), login, expected));
        assertEquals(expected, userService.findById(user.getId()).orElseThrow().getRole());
    }

    // regex: the authentication feature already has « L'utilisateur "ToTo" n'existe pas »
    @Et("L'utilisateur {string} n'existe pas")
    public void lUtilisateurNExistePas(String login) {
        assertTrue(user(login).isEmpty());
    }


    @Et("L'utilisateur {string} existe déjà")
    public void lUtilisateurExisteDeja(String login) {
        assertTrue(user(login).isPresent());
    }

    @Et("<login> a le mot de passe <mdp>")
    public void loginALeMotDePasseMdp() {
        throw new PendingException();
    }

    // ===== Quand =====

    @Quand("^Je change le role de \"?([^\" ]+)\"? pour le role \"?([^\"]+?)\"?$")
    public void jeChangeLeRole(String login, String gherkinRole) {
        String value = roleValue(gherkinRole);

        if (loggedAsAdmin && user(login).isPresent()) {
            // through the members page: the select of the user's row
            page.navigate(url("/members"));
            // the select only offers the possible roles: add the asked value if it is not there
            page.locator(row(login) + " .role-select").evaluate("""
                    (select, value) => {
                        if (![...select.options].some(option => option.value === value)) {
                            select.add(new Option(value, value));
                        }
                        select.value = value;
                    }""", value);
            page.waitForNavigation(() -> page.click(row(login) + " .role-btn"));
        } else {
            // unknown user or non-admin: the page does not offer it, the request is sent directly
            postForm("/members/" + idOf(login) + "/role", Map.of("role", value));
        }
    }

    @Quand("Je supprime {string} du site")
    public void jeSupprimeDuSite(String login) {
        if (loggedAsAdmin && user(login).isPresent()) {
            page.navigate(url("/members"));
            // the delete button asks for a confirmation
            page.onceDialog(Dialog::accept);
            page.waitForNavigation(() -> page.click(row(login) + " .delete-btn"));
        } else {
            postForm("/members/" + idOf(login) + "/delete", Map.of());
        }
    }

    @Quand("J'ajoute un utilisateur {string} en tant que {string} avec le mot de passe {string} avec comme année {string}")
    public void jAjouteUnUtilisateurAvecMotDePasseEtAnnee(String login, String role, String mdp, String year) {
        addUser(login, role, year);
    }

    @Quand("J'ajoute un utilisateur {string} en tant que {string}")
    public void jAjouteUnUtilisateurEnTantQue(String login, String role) {
        addUser(login, role, null);
    }

    @Quand("Je rentre les bons identifiants administateur")
    public void jeRentreLesBonsIdentifiantsAdministateur() {
        jeSuisConnecteEnTantQue("administrateur");
    }

    @Quand("Je rentre les mauvais identifiants administateur")
    public void jeRentreLesMauvaisIdentifiantsAdministateur() {
        Role role = role("administrateur");
        String sessionEmail = "session-" + role.name().toLowerCase() + "@example.com";

        if (userService.findByEmail(sessionEmail).isEmpty()) {
            createUser(sessionEmail, "session-" + role.name().toLowerCase(), role);
        }

        page.navigate(url("/login"));
        page.fill("#username", sessionEmail);
        page.fill("#password", WRONG_PASSWORD);
        page.click("#login-btn");
    }

    @Quand("Je me déconnecte")
    public void jeMeDéconnecte() {
        page.locator("#logout").click();
    }

    // ===== Alors =====

    @Alors("J'ai un message d'erreur qui dit que je ne peux pas changer le role d'un utilisateur en Administrateur")
    public void messageRoleAdministrateurInterdit() {
        assertTrue(visibleText().contains(message("error.user.role.change.admin")));
    }

    @Alors("J'ai un message d'erreur qui dit que le role n'existe pas")
    public void messageRoleInexistant() {
        assertTrue(visibleText().contains(message("error.user.role.invalid")));
    }

    @Alors("J'ai un message d'erreur qui dit que l'utilisateur n'existe pas")
    public void messageUtilisateurInexistant() {
        String text = visibleText();
        assertTrue(text.contains(message("error.user.role.change.notfound"))
                || text.contains(message("error.user.delete.notfound")));
    }

    @Alors("J'ai un message d'erreur qui dit que je n'ai pas le droit de changer les roles des utilisateur")
    public void messagePasLeDroitDeChangerLesRoles() {
        assertTrue(visibleText().contains(message("error.forbidden")));
        assertFalse(page.url().endsWith("/members"));
    }

    @Alors("J'ai un message d'erreur qui dit que je n'ai pas le droit de supprimer un utilisateur de site")
    public void messagePasLeDroitDeSupprimer() {
        assertTrue(visibleText().contains(message("error.forbidden")));
        assertFalse(page.url().endsWith("/members"));
    }

    @Alors("J'ai un message d'erreur qui dit qu'on ne peut pas ajouter un autre administrateur")
    public void messageAjoutRoleAdministrateurInterdit() {
        assertThat(page.locator("#role")).hasClass(Pattern.compile("is-invalid"));
        assertTrue(visibleText().contains(message("error.user.role.admin.forbidden")));
    }

    @Alors("L'utilisateur {string} existe")
    public void lUtilisateurLoginExiste(String login) {
        user(login).orElseGet(() -> createUser(email(login), login, Role.PROF));
        assertTrue(user(login).isPresent());
    }


    @Alors("{string} a le mot de passe {string}")
    public void a_le_mot_de_passe(String string, String string2) {
    }

    @Alors("J'ai un message d'erreur qui dit que l'utilisateur existe déjà")
    public void messageUtilisateurExisteDeja() {
        assertTrue(visibleText().contains(message("error.member.email.exists")));
    }

    @Alors("J'ai un message d'erreur qui dit que je n'ai pas le droit d'ajouter un utilisateur au site")
    public void messagePasLeDroitDAjouter() {
        assertTrue(visibleText().contains(message("error.forbidden")));
    }

    @Alors("J'ai un message d'erreur qui dit que les identifiants sont incorrects")
    public void messageErrorIdUncorrect() {
        assertThat(page.locator("#error")).containsText(message("error.login.generic"));
    }


}