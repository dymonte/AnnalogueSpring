package com.annalogue.m2.steps;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;


public class ModuleCreationAcceptanceTest {

    @Autowired
    private RegisterUserAcceptanceTest loginSteps;

    @Autowired
    private IModuleService moduleService;

    @Autowired
    private IInscriptionService inscriptionService;

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

    @LocalServerPort
    private int port;


    @After(order = 20000)
    public void cleanDatabase() {
        inscriptionService.deleteAll();
        moduleService.deleteAll();
        userService.deleteAll();
    }


    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Page page() {
        return loginSteps.getPage();
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.ENGLISH);
    }

    private void addModule(String name) {
        page().navigate(url("/modules/add"));
        page().locator("#name").fill(name);
        page().locator("form[action='/modules/add'] button[type=submit]").click();
    }

    private List<String> createdModuleNames() {
        page().navigate(url("/home?filter=created"));
        return page().locator(".module-name").allInnerTexts();
    }

    // 1

    @Quand("je crée un nouveau module intitulé {string}")
    public void IcreateModule(String name) {
        addModule(name);
        page().waitForURL(url("/home"));
    }

    @Alors("le module {string} est créé dans le catalogue")
    public void ModuleIsCreated(String name) {
        assertTrue(moduleService.existsByName(name));
        assertTrue(page().locator("body").innerText().contains(message("success.module.created", name)));
    }

    @Et("je suis désigné comme responsable du module {string}")
    public void IsResponsable(String name) {
        ModuleEntity module = moduleService.findByName(name).orElseThrow();
        assertEquals(loginSteps.getLoggedUser().getEmail(), module.getResponsible().getEmail());
    }

    @Et("le module {string} apparaît dans ma liste de modules gérés")
    public void ModuleIsInMyFields(String name) {
        assertTrue(createdModuleNames().contains(name));
    }


    // 2

    @Et("je gère déjà le module {string}")
    public void ImResponsableForOtherModules(String name) {
        moduleService.createModule(name, loginSteps.getLoggedUser());
    }

    @Et("les modules {string} et {string} apparaissent dans ma liste de gestion")
    public void AllModulesIsInMyFields(String first, String second) {
        List<String> names = createdModuleNames();
        assertTrue(names.contains(first));
        assertTrue(names.contains(second));
    }
}