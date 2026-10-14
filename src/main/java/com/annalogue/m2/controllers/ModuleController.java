package com.annalogue.m2.controllers;

import com.annalogue.m2.services.IModuleService;
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


// all texts are message keys of messages.properties: the pages display them with #{${...}}
@Controller
@RequestMapping("/modules")
public class ModuleController {

    private final IModuleService moduleService;

    public ModuleController(IModuleService moduleService) {
        this.moduleService = moduleService;
    }

    // list of all modules (catalogue)
    @GetMapping
    public String listModules(Model model) {
        model.addAttribute("modules", moduleService.findAll());
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
            ModuleEntity module = moduleService.createModule(name);

            // redirect after success, so refreshing the page does not create the module twice
            redirectAttributes.addFlashAttribute("successKey", "success.module.created");
            redirectAttributes.addFlashAttribute("createdModuleName", module.getName());
            return "redirect:/modules";

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

        model.addAttribute("module", module);
        return "modules/detail";
    }
}