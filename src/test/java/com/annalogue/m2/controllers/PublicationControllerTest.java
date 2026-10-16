package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.ldap.SecurityConfig;
import com.annalogue.m2.models.FileType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PublicationController.class)
@AutoConfigureMockMvc()
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class PublicationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private IPublicationService publicationService;

	@MockitoBean
	private IModuleService moduleService;

	@MockitoBean
	private IUserService userService;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private IInscriptionService inscriptionService;

    @MockitoBean
    private IFilesService filesService;

	@Test
	void shouldShowUploadPageForAdmin() throws Exception {

		UserEntity user = new UserEntity("admin@example.com", "hash", "admin", Role.ADMIN, null, "");
		ModuleEntity module = new ModuleEntity("MOIA", user);
		when(moduleService.findAll()).thenReturn(List.of(module));
		when(userService.findByEmail("admin@example.com")).thenReturn(Optional.of(
				new UserEntity("admin@example.com", "hash", "alice", Role.ADMIN, null, null)
		));

		mockMvc.perform(get("/upload").with(user("admin@example.com").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("modules/upload_file"))
				.andExpect(model().attributeExists("login"))
				.andExpect(model().attributeExists("isAdmin"))
				.andExpect(model().attributeExists("username"))
				.andExpect(model().attributeExists("fileType"))
				.andExpect(model().attributeExists("modules"));
	}

	@Test
	void shouldUploadFileAndRedirectWithSuccess() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"document.pdf",
				"application/pdf",
				"content".getBytes(StandardCharsets.UTF_8)
		);

		mockMvc.perform(multipart("/files")
						.file(file)
						.param("name", "My file")
						.param("description", "A PDF")
						.param("moduleId", "1")
						.param("fileType", FileType.CM.name())
						.with(user("admin@example.com").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/upload"))
				.andExpect(flash().attribute("successKey", "success.upload.file"));

		verify(publicationService).saveMultipartFile(
				any(),
				eq("My file"),
				eq("A PDF"),
				eq(1L),
				eq("admin@example.com"),
				eq(FileType.CM)
		);
	}

	@Test
	void shouldRedirectBackWithErrorWhenUploadFails() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"document.pdf",
				"application/pdf",
				"content".getBytes(StandardCharsets.UTF_8)
		);

		doThrow(new UploadFileException("error.upload.failed"))
				.when(publicationService)
				.saveMultipartFile(any(), eq("My file"), eq("A PDF"), eq(1L), eq("admin@example.com"), eq(FileType.CM));

		mockMvc.perform(multipart("/files")
						.file(file)
						.param("name", "My file")
						.param("description", "A PDF")
						.param("moduleId", "1")
						.param("fileType", FileType.CM.name())
						.with(user("admin@example.com").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/upload"))
				.andExpect(flash().attribute("errorKey", "error.upload.failed"))
				.andExpect(flash().attribute("name", "My file"))
				.andExpect(flash().attribute("description", "A PDF"))
				.andExpect(flash().attribute("moduleId", 1L))
				.andExpect(flash().attribute("fileType", FileType.CM.name()));
	}

	@Test
	void shouldReturnNotFoundWhenRequestedFileDoesNotExist() throws Exception {
		when(publicationService.loadAsResource(99L))
				.thenThrow(new StorageFileNotFoundException());

		mockMvc.perform(get("/files/99").with(user("admin@example.com").roles("ADMIN")))
				.andExpect(status().isNotFound());
	}
}
