package com.annalogue.m2.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Controller
public class AdminController {

	  @RequestMapping("/admin")
	  public String admin(Authentication authentication, Model model) {

		  model.addAttribute("login", authentication != null ? authentication.getName() : "");
		  model.addAttribute("isAdmin", authentication != null && authentication.getAuthorities().stream()
				  .anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN")));
		  return "admin";
	  }
}
