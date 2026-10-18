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

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

// 1341 (NOTIF-01), 1342 (NOTIF-02), 1343 (NOTIF-03)
// some sentences are also used by 1346 (deactivation): reuse them, do not write them again
public class NotificationAcceptanceTest {

    private static final String PASSWORD = "Passw0rd!";
    private static final String RESP_EMAIL = "resp-notif@example.com";

    // gives us the browser page (opened before each scenario)
    @Autowired
    private RegisterUserAcceptanceTest loginSteps;

    @Autowired
    private IUserService userService;

    @Autowired
    private IModuleService moduleService;

    @Autowired
    private IInscriptionService inscriptionService;

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

    // 1
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

}