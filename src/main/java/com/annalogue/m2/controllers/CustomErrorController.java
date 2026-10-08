package com.annalogue.m2.controllers;

import com.annalogue.m2.services.NotifyService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Controller
@Primary
public class CustomErrorController implements ErrorController {

	private final NotifyService notifyService;

	public CustomErrorController(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Authentication authentication, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());
			request.setAttribute("error_code", statusCode);
			request.setAttribute("error_message", errorMessage(statusCode));
		}

		model.addAttribute("login", authentication != null ? authentication.getName() : "");
		model.addAttribute("isAdmin", authentication != null && authentication.getAuthorities().stream()
				.anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN")));
		return "error";
	}

	// HTTP status -> message from messages.properties
	private String errorMessage(int statusCode) {
		if (statusCode == HttpStatus.NOT_FOUND.value()) {
			return notifyService.getMessage("error.page.notfound");
		}
		if (statusCode == HttpStatus.FORBIDDEN.value()) {
			return notifyService.getMessage("error.page.forbidden");
		}
		if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
			return notifyService.getMessage("error.page.server");
		}
		return notifyService.getMessage(NotifyService.DEFAULT_ERROR_KEY);
	}
}