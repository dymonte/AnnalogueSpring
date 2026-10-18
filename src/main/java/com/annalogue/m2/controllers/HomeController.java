package com.annalogue.m2.controllers;

import com.annalogue.m2.dto.UserModuleView;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.services.HandleUserService;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IUserService;
import com.annalogue.m2.services.RoleService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    // filters of « My modules »
    static final String FILTER_ALL = "all";
    static final String FILTER_CREATED = "created";
    static final String FILTER_ENROLLED = "enrolled";

    private final IUserService userService;
    private final IModuleService moduleService;
    private final RoleService roleService;
    private final HandleUserService handleUserService;

    public HomeController(IUserService userService, IModuleService moduleService, RoleService roleService, HandleUserService handleUserService) {
        this.userService = userService;
        this.moduleService = moduleService;
        this.roleService = roleService;
        this.handleUserService = handleUserService;
    }

    // the admin keeps his home page; for the others the home page is « My modules »
    @GetMapping(value = { "/home", "/" })
    public String home(@RequestParam(defaultValue = FILTER_ALL) String filter,
                       @RequestParam(required = false) String role,
                       Authentication authentication,
                       Model model) {

        String email = authentication != null ? authentication.getName() : "";
        boolean isAdmin = hasAnyRole(authentication, "ROLE_ADMIN");

        model.addAttribute("login", email);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("canCreateModuleAndPublication",
                hasAnyRole(authentication, "ROLE_ADMIN", "ROLE_PROF", "ROLE_RESPONSABLE"));
        model.addAttribute("canCreateModule", hasAnyRole(authentication, "ROLE_RESPONSABLE"));

        if (authentication != null) {
            userService.findByEmail(email).ifPresent(
                    userEntity -> model.addAttribute("username", userEntity.getUsername()));
        }

        if (isAdmin) {
            Role selectedRole = (role != null && !role.isBlank() && roleService.exists(role))
                    ? roleService.fromString(role)
                    : null;

            model.addAttribute("users", selectedRole == null
                    ? handleUserService.findAllUsers()
                    : handleUserService.findUsersByRole(selectedRole));
            model.addAttribute("filterRoles", roleService.getRoles());
            model.addAttribute("selectedFilter", selectedRole == null ? "" : selectedRole.name());
            model.addAttribute("teacherRoles", roleService.getTeacherRoles());

            return "home";
        }

        addMyModules(model, moduleService.findModulesOfUser(email), filter);
        return "modules/list";
    }

    // the modules of the user, filtered: all, created by him, or the ones he is enrolled in
    private void addMyModules(Model model, List<UserModuleView> modules, String filter) {
        List<UserModuleView> created = modules.stream()
                .filter(UserModuleView::isResponsible)
                .toList();
        List<UserModuleView> enrolled = modules.stream()
                .filter(module -> !module.isResponsible())
                .toList();

        // an unknown filter shows everything
        String selectedFilter = switch (filter) {
            case FILTER_CREATED, FILTER_ENROLLED -> filter;
            default -> FILTER_ALL;
        };

        List<UserModuleView> shown = switch (selectedFilter) {
            case FILTER_CREATED -> created;
            case FILTER_ENROLLED -> enrolled;
            default -> modules;
        };

        model.addAttribute("modules", shown);
        model.addAttribute("filter", selectedFilter);
        model.addAttribute("countAll", modules.size());
        model.addAttribute("countCreated", created.size());
        model.addAttribute("countEnrolled", enrolled.size());
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> Arrays.asList(roles).contains(authority.getAuthority()));
    }
}
