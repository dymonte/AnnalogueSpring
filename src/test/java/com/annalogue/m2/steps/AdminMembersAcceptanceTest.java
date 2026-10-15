package com.annalogue.m2.steps;

import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// ADMIN-07: 817_Supression_utilisateur.feature
// ADMIN-02: 820_Gestion_des_roles.feature
public class AdminMembersAcceptanceTest {

    private static final String PASSWORD = "Passw0rd!";
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

    // quotes are optional: the examples of 820 write ERM and Membre without quotes.
    // before an action it creates the user; after an action it checks his role
    @Et("^\"?([^\" ]+)\"? a le role \"?([^\"]+?)\"?$")
    public void aLeRole(String login, String gherkinRole) {
        Role expected = role(gherkinRole);
        UserEntity user = user(login).orElseGet(() -> createUser(email(login), login, expected));
        assertEquals(expected, userService.findById(user.getId()).orElseThrow().getRole());
    }

    // regex: the authentication feature already has « L'utilisateur "ToTo" n'existe pas »
    @Et("^L'utilisateur \"(?!ToTo\")([^\"]*)\" n'existe pas$")
    public void lUtilisateurNExistePas(String login) {
        assertTrue(user(login).isEmpty());
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
}