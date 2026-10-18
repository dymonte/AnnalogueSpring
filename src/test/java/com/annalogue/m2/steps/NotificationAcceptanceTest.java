package com.annalogue.m2.steps;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.NotificationType;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.NotificationRepository;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.INotificationService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.annalogue.m2.services.IPublicationService;
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationAcceptanceTest {

    private static final String PASSWORD = "Passw0rd!";
    private static final String RESP_EMAIL = "resp-notif@example.com";
    private static final String ADMIN_EMAIL = "notif-admin@example.com";

    @Autowired
    private RegisterUserAcceptanceTest loginSteps;

    @Autowired
    private IUserService userService;

    @Autowired
    private IModuleService moduleService;

    @Autowired
    private IInscriptionService inscriptionService;

    @Autowired
    private IPublicationService publicationService;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    @After(order = 30000)
    public void cleanDatabase() {
        notificationRepository.deleteAll();
        inscriptionService.deleteAll();
        moduleService.deleteAll();
        userService.deleteAll();
    }

    // help methodes

    private Page page() {
        return loginSteps.getPage();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private UserEntity createUser(String email, Role role) {
        String username = email.substring(0, email.indexOf('@'));
        return userService.save(new UserEntity(email, passwordEncoder.encode(PASSWORD), username, role, null, null));
    }

    private void login(String email) {
        page().context().clearCookies();
        page().navigate(url("/login"));
        page().locator("#username").fill(email);
        page().locator("#password").fill(PASSWORD);
        page().locator("#login-btn").click();
        page().waitForURL(url("/home"));
    }

    private boolean isEnrolled(String email, String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName).get();
        for (InscriptionEntity inscription : inscriptionService.findByModuleId(module.getId())) {
            if (inscription.getUser().getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNotification(String email, NotificationType type, String content) {
        UserEntity user = userService.findByEmail(email).get();
        for (NotificationEntity notification : notificationService.findForUser(user.getId())) {
            if (notification.getType() == type && Objects.equals(notification.getContent(), content)) {
                return true;
            }
        }
        return false;
    }

    private String notificationsPageText(String email) {
        login(email);
        page().navigate(url("/notifications"));
        return page().locator("main").innerText();
    }

    // ---------------------- 1


    @Soit("un module {string} existe")
    public void moduleExist(String moduleName) {
        UserEntity responsable = createUser(RESP_EMAIL, Role.RESPONSABLE);
        moduleService.createModule(moduleName, responsable);

        assertTrue(moduleService.existsByName(moduleName));
    }
    @Et("{string} n'a pas désactivées les notifications")
    public void notificationsOn(String email) {
        UserEntity user = createUser(email, Role.ETUDIANT);

        assertTrue(user.isNotificationsEnabled());
    }



    @Et("{string} n'est pas inscrit au module {string}")
    public void UserNotSub(String email, String moduleName) {
        assertFalse(isEnrolled(email, moduleName));
    }

    @Quand("le responsable du module {string} inscrit {string} au module")
    public void RespAddUserToModule(String moduleName, String email) {
        ModuleEntity module = moduleService.findByName(moduleName).get();
        login(RESP_EMAIL);

        page().navigate(url("/module-enrollments/new"));
        page().locator("#moduleId").selectOption(String.valueOf(module.getId()));
        page().locator("#emailUser").fill(email);
        page().locator("#enrolle-btn").click();
        page().waitForURL(url("/module-enrollments"));

        assertTrue(isEnrolled(email, moduleName));
    }

    @Alors("le système doit notifier {string} qu'il est inscrit au module {string}")
    public void UserNotifiedBySub(String email, String moduleName) {
        assertTrue(hasNotification(email, NotificationType.MODULE_ENROLLMENT, moduleName));

        String pageText = notificationsPageText(email);
        assertTrue(pageText.contains("You were enrolled in the module " + moduleName + "."));
    }


    //---------------------- 2

    @Soit("l'administrateur est connecté")
    public void adminIsLoggedIn() {
        createUser(ADMIN_EMAIL, Role.ADMIN);
        login(ADMIN_EMAIL);
    }
    @Quand("l'administrateur inscrit l'utilisateur {string}")
    public void AdminAddUser(String email) {
        page().navigate(url("/members/add"));
        page().locator("#email").fill(email);
        page().locator("#username").fill("user");
        page().locator("#role").selectOption("PROF");
        page().locator("button:has-text('Add Member')").click();
        page().locator(".alert-success").waitFor();

        UserEntity created = userService.findByEmail(email).get();
        created.setPasswordHash(passwordEncoder.encode(PASSWORD));
        userService.save(created);
    }

    @Alors("le système doit notifier {string} qu'il est inscrit à l'application")
    public void NotifiedAccountCreated(String email) {
        assertTrue(hasNotification(email, NotificationType.ACCOUNT_CREATED, null));

        String pageText = notificationsPageText(email);
        assertTrue(pageText.contains("Welcome! The administrator created your account."));
    }


    // ------------------------ 3

    @Et("{string} est inscrit au module {string}")
    public void UserSub(String email, String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName).get();
        UserEntity responsable = userService.findByEmail(RESP_EMAIL).get();
        inscriptionService.save(responsable, email, module.getId());

        assertTrue(isEnrolled(email, moduleName));
    }


    @Quand("le responsable du module {string} ajoute un sujet")
    public void RespAddsSujet(String moduleName) {
        ModuleEntity module = moduleService.findByName(moduleName).get();
        login(RESP_EMAIL);

        page().navigate(url("/upload"));
        byte[] pdf = "%PDF-1.4 sujet".getBytes(StandardCharsets.UTF_8);
        page().locator("#fileInput").setInputFiles(new FilePayload("sujet.pdf", "application/pdf", pdf));
        page().locator("#name").fill("Sujet");
        page().locator("#description").fill("Exam subject");
        page().locator("#module").selectOption(String.valueOf(module.getId()));
        page().locator("#fileType").selectOption("EXAM");
        page().locator("form[action='/files'] button[type=submit]").click();
        page().locator(".alert").first().waitFor();
        String alertText = page().locator(".alert").first().innerText();
        assertTrue(page().locator(".alert-success").count() > 0, "Upload failed: " + alertText);

        assertTrue(publicationService.findByName("Sujet").isPresent());
    }



    @Alors("le système doit notifier {string} qu'un sujet a été ajouté au module {string}")
    public void NotifiedSujetAdded(String email, String moduleName) {
        assertTrue(hasNotification(email, NotificationType.PUBLICATION_ADDED, moduleName));

        String pageText = notificationsPageText(email);
        assertTrue(pageText.contains("A new subject was added to the module " + moduleName + "."));
    }
}