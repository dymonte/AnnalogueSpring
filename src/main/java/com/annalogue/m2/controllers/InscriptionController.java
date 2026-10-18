package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.EmailNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotFoundException;
import com.annalogue.m2.exceptions.ModuleNotManagedException;
import com.annalogue.m2.exceptions.UserAlreadyEnrolledException;
import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.IInscriptionService;
import com.annalogue.m2.services.INotifyService;
import com.annalogue.m2.services.IUserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import com.annalogue.m2.dto.GroupEnrollmentResult;

@Controller
public class InscriptionController {

        private static final String FORM_VIEW = "modules/add_member_module";

        private final IUserService userService;
        private final IInscriptionService inscriptionService;
        private final INotifyService notifyService;

        public InscriptionController(
                        IUserService userService,
                        IInscriptionService inscriptionService,
                        INotifyService notifyService) {

                this.userService = userService;
                this.inscriptionService = inscriptionService;
                this.notifyService = notifyService;
        }

        @GetMapping("/module-enrollments/new")
        public String showEnrollmentForm(Model model) {
                getModules(model);
                return FORM_VIEW;
        }

        @PostMapping("/module-enrollments/new")
        public String registerUser(
                        @RequestParam Long moduleId,
                        @RequestParam String emailUser,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                try {
                        UserEntity currentUser = getCurrentUser();

                        inscriptionService.save(
                                        currentUser,
                                        emailUser,
                                        moduleId);

                        redirectAttributes.addFlashAttribute(
                                        "success",
                                        notifyService.getMessage(
                                                        "success.inscription.created"));

                        return "redirect:/module-enrollments";

                } catch (ModuleNotFoundException e) {

                        addError(
                                        model,
                                        "moduleIdError",
                                        e.getMessageKey());

                } catch (ModuleNotManagedException e) {

                        addError(
                                        model,
                                        "moduleIdError",
                                        e.getMessageKey());

                } catch (EmailNotFoundException e) {

                        addError(
                                        model,
                                        "emailUserError",
                                        e.getMessageKey());

                } catch (UserAlreadyEnrolledException e) {

                        addError(
                                        model,
                                        "userAlreadyRegisterError",
                                        e.getMessageKey());

                } catch (UsernameNotFoundException e) {

                        addError(
                                        model,
                                        "error",
                                        "error.inscription.prof.not.found");

                } catch (IllegalArgumentException e) {

                        String key = e.getMessage();

                        switch (key) {
                                case "error.inscription.user.role.invalid", "error.inscription.email.invalid",
                                                "error.inscription.email.required" ->
                                        addError(model, "emailUserError", key);
                                case "error.inscription.module.required" -> addError(
                                                model,
                                                "moduleIdError",
                                                key);
                                case null, default -> addError(
                                                model,
                                                "error",
                                                "error.request.invalid");
                        }

                } catch (Exception e) {

                        addError(
                                        model,
                                        "error",
                                        "error.server");
                }

                return reloadForm(model, emailUser, moduleId);
        }

        // TFAN26-106: registers all the members of a group in a module
        @PostMapping("/module-enrollments/group")
        public String registerGroup(
                        @RequestParam(required = false) Long moduleId,
                        @RequestParam(required = false) String group,
                        RedirectAttributes redirectAttributes) {

                try {
                        UserEntity currentUser = getCurrentUser();

                        GroupEnrollmentResult result = inscriptionService.saveGroup(currentUser, group, moduleId);

                        String message = result.alreadyRegisteredNames().isEmpty()
                                        ? notifyService.getMessage("success.inscription.group.created",
                                                        result.added(), result.alreadyRegistered())
                                        : notifyService.getMessage("success.inscription.group.created.already",
                                                        result.added(), result.alreadyRegistered(),
                                                        String.join(", ", result.alreadyRegisteredNames()));

                        redirectAttributes.addFlashAttribute("success", message);

                } catch (ModuleNotFoundException e) {
                        addFlashError(redirectAttributes, e.getMessageKey());

                } catch (ModuleNotManagedException e) {
                        addFlashError(redirectAttributes, e.getMessageKey());

                } catch (UsernameNotFoundException e) {
                        addFlashError(redirectAttributes, "error.inscription.prof.not.found");

                } catch (IllegalArgumentException e) {
                        String key = e.getMessage();

                        switch (key) {
                                case "error.inscription.module.required",
                                                "error.inscription.group.required",
                                                "error.inscription.group.not.found" ->
                                        addFlashError(redirectAttributes, key);
                                case null, default ->
                                        addFlashError(redirectAttributes, "error.request.invalid");
                        }

                } catch (Exception e) {
                        addFlashError(redirectAttributes, "error.server");
                }

                return "redirect:/module-enrollments";
        }

        @PostMapping("/module-enrollments/{id}/delete")
        public String removeEnrollment(
                        @PathVariable Long id,
                        RedirectAttributes redirectAttributes) {
            try {
                UserEntity currentUser = getCurrentUser();

                if (!inscriptionService.existsById(id)) {
                    redirectAttributes.addFlashAttribute(
                            "error",
                            notifyService.getMessage("error.inscription.not.enrolled")

                    );
                    return "redirect:/module-enrollments";
                }

                boolean isManagedEnrollment =
                        inscriptionService.findInscriptions(currentUser).stream()
                                .anyMatch(inscription -> inscription.getId().equals(id));

                if (!isManagedEnrollment) {
                    redirectAttributes.addFlashAttribute(
                            "error",
                            notifyService.getMessage("error.inscription.not.found")
                    );
                    return "redirect:/module-enrollments";
                }

                inscriptionService.deleteById(id);

                redirectAttributes.addFlashAttribute(
                        "success",
                        notifyService.getMessage("success.inscription.deleted")
                );

            } catch (UsernameNotFoundException e) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        notifyService.getMessage("error.inscription.prof.not.found")
                );
            }

            return "redirect:/module-enrollments";
        }

        @PostMapping("/module-enrollments/{id}/toggle-editor")
        public String toggleEditor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
                try {
                        UserEntity currentUser = getCurrentUser();
                    if (!inscriptionService.existsById(id)) {
                        redirectAttributes.addFlashAttribute(
                                "error",
                                notifyService.getMessage("error.inscription.not.enrolled")

                        );
                        return "redirect:/module-enrollments";
                    }

                    boolean isManagedEnrollment =
                            inscriptionService.findInscriptions(currentUser).stream()
                                    .anyMatch(inscription -> inscription.getId().equals(id));

                    if (!isManagedEnrollment) {
                        redirectAttributes.addFlashAttribute(
                                "error",
                                notifyService.getMessage("error.inscription.not.found")
                        );
                        return "redirect:/module-enrollments";
                    }
                        boolean isEditor = inscriptionService.toggleEditor(id);

                        String messageKey = isEditor
                                        ? "success.inscription.editor.enabled"
                                        : "success.inscription.editor.disabled";

                        redirectAttributes.addFlashAttribute(
                                        "success",
                                        notifyService.getMessage(messageKey));

                } catch (UsernameNotFoundException e) {
                        redirectAttributes.addFlashAttribute(
                                        "error",
                                        notifyService.getMessage("error.inscription.prof.not.found"));
                }
                return "redirect:/module-enrollments";
        }

        @GetMapping("/module-enrollments")
        public String listModuleEnrollments(Model model) {
                UserEntity currentUser = getCurrentUser();

                List<InscriptionEntity> inscriptions = inscriptionService.findInscriptions(currentUser);

                model.addAttribute("inscriptions", inscriptions);

                return "modules/register_members_view";
        }

        private UserEntity getCurrentUser() {
                Authentication auth = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (auth == null
                                || !auth.isAuthenticated()
                                || auth.getName() == null
                                || "anonymousUser".equals(auth.getName())) {

                        throw new UsernameNotFoundException(
                                        "error.inscription.prof.not.found");
                }

                return userService.findByEmail(auth.getName())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "error.inscription.prof.not.found"));
        }

        private void getModules(Model model) {
                try {
                        UserEntity currentUser = getCurrentUser();

                        List<ModuleEntity> modules = currentUser.getResponsibleModules();

                        model.addAttribute("modules", modules);

                } catch (UsernameNotFoundException e) {

                        model.addAttribute(
                                        "modules",
                                        Collections.emptyList());

                        addError(
                                        model,
                                        "error",
                                        "error.inscription.prof.not.found");
                }
        }

        private String reloadForm(
                        Model model,
                        String emailUser,
                        Long moduleId) {

                model.addAttribute("emailUser", emailUser);
                model.addAttribute("selectedModuleId", moduleId);

                getModules(model);

                return FORM_VIEW;
        }

        private void addError(
                        Model model,
                        String attributeName,
                        String messageKey) {

                model.addAttribute(
                                attributeName,
                                notifyService.getMessage(messageKey));
        }

        private void addFlashError(RedirectAttributes redirectAttributes, String messageKey) {
                redirectAttributes.addFlashAttribute(
                                "error",
                                notifyService.getMessage(messageKey));
        }
}