package com.annalogue.m2.steps;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.CommentRepository;
import com.annalogue.m2.repositories.CorrectionRepository;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IPublicationService;
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
    private UserAcceptanceTest userSteps;

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

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CorrectionRepository correctionRepository;

    @Autowired
    private IPublicationService publicationService;

    @LocalServerPort
    private int port;


    @After(order = 20000)
    public void cleanDatabase() {
        commentRepository.deleteAllInBatch();
        correctionRepository.deleteAllInBatch();
        publicationService.deleteAll();
        inscriptionService.deleteAll();
        moduleService.deleteAll();
        userService.deleteAll();
    }


    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Page page() {
        return userSteps.getPage();
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
        assertEquals(userSteps.getCurrentUser().getEmail(), module.getResponsible().getEmail());
    }

    @Et("le module {string} apparaît dans ma liste de modules gérés")
    public void ModuleIsInMyFields(String name) {
        assertTrue(createdModuleNames().contains(name));
    }


    // 2

    @Et("je gère déjà le module {string}")
    public void ImResponsableForOtherModules(String name) {
        moduleService.createModule(name, userSteps.getCurrentUser());
    }

    @Et("les modules {string} et {string} apparaissent dans ma liste de gestion")
    public void AllModulesIsInMyFields(String first, String second) {
        List<String> names = createdModuleNames();
        assertTrue(names.contains(first));
        assertTrue(names.contains(second));
    }


    // 3

    @Quand("je tente d'accéder à la création de module")
    public void AccesAddModule() {
        page().navigate(url("/modules/add"));
    }

    @Et("un message d'erreur indique que seuls les responsables de module peuvent effectuer cette action")
    public void AccesRefuseMessage() {
        assertEquals("403", page().locator("#error-code").innerText().trim());
        assertEquals(message("error.forbidden"), page().locator("#forbidden-msg").innerText().trim());
    }

    // 4

    @Quand("je valide le formulaire sans renseigner de titre de module")
    public void ValidFormwithoutName() {
        page().navigate(url("/modules/add"));
        page().locator("#name").evaluate("input => input.removeAttribute('required')");
        page().locator("form[action='/modules/add'] button[type=submit]").click();
        page().locator("#name-error").waitFor();
    }

    @Alors("le module n'est pas créé")
    public void ModuleIsntCreated() {
        assertTrue(moduleService.findAll().isEmpty());
    }

    @Et("un message d'erreur signale que le nom du module est obligatoire")
    public void ErrorMessageNotcreated() {
        assertEquals(message("error.module.name.empty"), page().locator("#name-error").innerText().trim());
    }

    // 5

    @Soit("le module {string} déjà existant sur la plateforme")
    public void ModuleExist(String name) {
        UserEntity other = userService.save(new UserEntity(
                "autre.responsable@example.com", passwordEncoder.encode("Passw0rd!"),
                "autreresp", Role.RESPONSABLE, null, null));
        moduleService.createModule(name, other);
    }

    @Quand("je tente de créer à nouveau un module nommé {string}")
    public void CreateAnotherModule(String name) {
        addModule(name);
        page().locator("#name-error").waitFor();
    }

    @Alors("le second module n'est pas créé")
    public void AnotherModuleNotCreated() {
        assertEquals(1, moduleService.findAll().size());
    }

    @Et("un message d'erreur signale que le module {string} existe déjà")
    public void ErrorMessageExistModule(String name) {
        assertEquals(message("error.module.name.exists"), page().locator("#name-error").innerText().trim());
        assertEquals(name, page().locator("#name").inputValue());
    }
}