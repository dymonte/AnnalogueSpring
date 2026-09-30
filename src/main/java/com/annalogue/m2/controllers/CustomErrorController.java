package com.annalogue.m2.controllers;

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
	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Authentication authentication, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());

			if(statusCode == HttpStatus.NOT_FOUND.value()) {
				request.setAttribute("error_code", 404);
				request.setAttribute("error_message", "Page not found");
			}
			else if(statusCode == HttpStatus.FORBIDDEN.value()) {
				request.setAttribute("error_code", 403);
				request.setAttribute("error_message", "Access denied");
			}
			else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				request.setAttribute("error_code", 500);
				request.setAttribute("error_message", "Internal server error");
			}
			else {
				request.setAttribute("error_code", statusCode);
				request.setAttribute("error_message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
			}
		}

		model.addAttribute("login", authentication != null ? authentication.getName() : "");
		model.addAttribute("isAdmin", authentication != null && authentication.getAuthorities().stream()
				.anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN")));
		return "error";
	}
}
