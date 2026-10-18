package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.annalogue.m2.models.ModuleEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.annalogue.m2.exceptions.InvalidModuleDataException;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;


// all texts are message keys of messages.properties: the pages display them with #{${...}}
@Controller
@RequestMapping("/modules")
public class ModuleController {

    private final IModuleService moduleService;
    private final IUserService userService;
    private final IInscriptionService inscriptionService;

    public ModuleController(IModuleService moduleService, IUserService userService, IInscriptionService inscriptionService) {
        this.moduleService = moduleService;
        this.userService = userService;
        this.inscriptionService = inscriptionService;
    }

    // catalogue of all the modules, for the admin only;
    // the teachers and the students see their modules on their home page
    @GetMapping
    public String listModules(Authentication authentication, Model model) {
        if (!isAdmin(authentication)) {
            return "redirect:/home";
        }

        List<UserModuleView> modules = moduleService.findAllModuleViews();

        model.addAttribute("modules", modules);
        model.addAttribute("filter", "all");
        model.addAttribute("countAll", modules.size());
        model.addAttribute("countCreated", 0);
        model.addAttribute("countEnrolled", 0);
        model.addAttribute("login", authentication.getName());
        model.addAttribute("isAdmin", true);
        return "modules/list";
    }

    // empty creation form
    @GetMapping("/add")
    public String showAddForm() {
        return "modules/add";
    }

    @PostMapping("/add")
    public String addModule(@RequestParam(required = false) String name,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            UserEntity responsible = userService.getCurrentUser();
            ModuleEntity module = moduleService.createModule(name, responsible);

            // redirect after success, so refreshing the page does not create the module twice
            redirectAttributes.addFlashAttribute("successKey", "success.module.created");
            redirectAttributes.addFlashAttribute("createdModuleName", module.getName());
            // back to the home page, where the new module is listed
            return "redirect:/home";

        } catch (InvalidModuleDataException e) {
            // empty, too long or already used name: shown under the name field
            model.addAttribute("nameErrorKey", e.getMessageKey());

        } catch (DataAccessException e) {
            // database problem: shown as a general alert
            model.addAttribute("errorKey", "error.module.database");
        }

        // show the form again with what the user typed
        model.addAttribute("name", name);
        return "modules/add";
    }

    // page of one module
    @GetMapping("/{id}")
    public String showModule(@PathVariable Long id, Model model) {
        ModuleEntity module = moduleService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity user = userService.getCurrentUser();
        boolean isResponsible = Objects.equals(module.getResponsible().getId(), user.getId());
        if (!isResponsible && !inscriptionService.hasReaderAccess(user, module)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("module", module);

        model.addAttribute("publications", module.getPublications());
        return "modules/detail";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}