package com.annalogue.m2.controllers;

import com.annalogue.m2.services.IModuleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// all texts are message keys of messages.properties: the pages display them with #{${...}}
@Controller
@RequestMapping("/modules")
public class ModuleController {

    private final IModuleService moduleService;

    public ModuleController(IModuleService moduleService) {
        this.moduleService = moduleService;
    }

    // empty creation form
    @GetMapping("/add")
    public String showAddForm() {
        return "modules/add";
    }
}