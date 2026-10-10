package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.FileType;
import com.annalogue.m2.services.IModuleService;
import com.annalogue.m2.services.IPublicationService;
import com.annalogue.m2.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
public class PublicationController {

	@Autowired
	private IPublicationService publicationService;

	@Autowired
	private IModuleService moduleService;

	@Autowired
	private IUserService userService;

	@PostMapping("/publications")
	public String addPublication() {
		return "publications";
	}

	@GetMapping("/files/{id}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable Long id) {
		try {
			Resource file = publicationService.loadAsResource(id);
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"");
			headers.setContentType(MediaType.APPLICATION_PDF);

			return new ResponseEntity<>(file, headers, HttpStatus.OK);
		}
		catch (StorageFileNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/upload")
	public String showFileUploadForm(Authentication authentication, Model model, @RequestParam(required = false) String error,
									  @RequestParam(required = false) String name,
									  @RequestParam(required = false) String description,
									  @RequestParam(required = false) Long moduleId,
									  @RequestParam(required = false) String fileType) {

		model.addAttribute("login", authentication != null ? authentication.getName() : "");
		model.addAttribute("isAdmin",
				authentication != null && authentication.getAuthorities() != null
						&& authentication.getAuthorities().stream()
						.anyMatch(role -> Objects.equals(role.getAuthority(), "ROLE_ADMIN")));

		if (authentication != null) {
			userService.findByEmail(authentication.getName()).ifPresent(
					userEntity -> model.addAttribute("username", userEntity.getUsername()));
		}


		model.addAttribute("fileType", FileType.values());
		model.addAttribute("modules", moduleService.findAll());
		if (error != null) {
			model.addAttribute("error", error);
		}
		if (name != null) {
			model.addAttribute("name", name);
		}
		if (description != null) {
			model.addAttribute("description", description);
		}
		if (moduleId != null) {
			model.addAttribute("moduleId", moduleId);
		}
		if (fileType != null) {
			model.addAttribute("selectedFileType", fileType);
		}
		return "modules/upload_file";
	}

	@PostMapping("/files")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
								   @RequestParam("name") String name,
								   @RequestParam("description") String description,
								   @RequestParam("moduleId") Long moduleId,
								   @RequestParam("fileType") FileType fileType,
								   Authentication authentication,
								   RedirectAttributes redirectAttributes) {

		String authorEmail = authentication.getName();

		if (file.getSize() > 10 * 1024 * 1024) {
			redirectAttributes.addAttribute("error", "Le fichier dépasse la taille maximale autorisée de 10MB.");
			redirectAttributes.addAttribute("name", name);
			redirectAttributes.addAttribute("description", description);
			redirectAttributes.addAttribute("moduleId", moduleId);
			redirectAttributes.addAttribute("fileType", fileType.name());
			return "redirect:/upload";
		}

		try {
			publicationService.saveMultipartFile(file, name, description, moduleId, authorEmail, fileType);
		} catch (UploadFileException e) {
			redirectAttributes.addAttribute("error", e.getMessage());
			redirectAttributes.addAttribute("name", name);
			redirectAttributes.addAttribute("description", description);
			redirectAttributes.addAttribute("moduleId", moduleId);
			redirectAttributes.addAttribute("fileType", fileType.name());
			return "redirect:/upload";
		}

		redirectAttributes.addFlashAttribute("success", "Le fichier a été téléchargé avec succès.");
		return "redirect:/upload";
	}

}
