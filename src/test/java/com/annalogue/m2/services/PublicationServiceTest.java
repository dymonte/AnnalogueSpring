package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.FileType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.PublicationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublicationServiceTest {

	@Mock
	private PublicationRepository publicationRepository;

	@Mock
	private IUserService userService;

	@Mock
	private IModuleService moduleService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private IInscriptionService inscriptionService;

	@InjectMocks
	private PublicationService publicationService;
	private AutoCloseable mocks;

	@BeforeEach
	public void setUp() throws Exception {
		mocks = MockitoAnnotations.openMocks(this);
		Field uploadDirField = PublicationService.class.getDeclaredField("uploadDir");
		uploadDirField.setAccessible(true);
		uploadDirField.set(publicationService, "/tmp/test-uploads");

		File testUploadDir = new File("/tmp/test-uploads");
		if (!testUploadDir.exists()) {
			testUploadDir.mkdirs();
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		File testUploadDir = new File("/tmp/test-uploads");
		if (testUploadDir.exists()) {
			deleteDirectory(testUploadDir);
		}
		mocks.close();
	}

	private void deleteDirectory(File directory) {
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		directory.delete();
	}

	@Test
	void testAddPublication() throws UploadFileException {
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getOriginalFilename()).thenReturn("testFile.pdf");
		Mockito.when(multipartFile.getSize()).thenReturn(100L);

		ModuleEntity moduleEntity = new ModuleEntity();
		Mockito.when(moduleService.findById(Mockito.anyLong())).thenReturn(Optional.of(moduleEntity));

		UserEntity userEntity = new UserEntity();
		Mockito.when(userService.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));

		Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

		Mockito.when(inscriptionService.hasWriterAccess(Mockito.any(), Mockito.any())).thenReturn(true);

		publicationService.saveMultipartFile(multipartFile, "testFile", "testDescription", 1L, "test@test.com", FileType.CM);

		Mockito.verify(publicationRepository, Mockito.times(1)).save(Mockito.any(PublicationEntity.class));
	}

	@Test
	void testLoadAsResource() throws IOException {
		File testFile = new File("/tmp/test-uploads/testFile.pdf");
		testFile.createNewFile();

		PublicationEntity publicationEntity = new PublicationEntity();
		publicationEntity.setFilePath("/tmp/test-uploads/testFile.pdf");
		Mockito.when(publicationRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(publicationEntity));

		publicationService.loadAsResource(1L);

		Mockito.verify(publicationRepository, Mockito.times(1)).findById(Mockito.anyLong());

	}


	@Test
	void testLoadAsResourceWithNotFoundException() {
		assertThrows(StorageFileNotFoundException.class, () -> {
			publicationService.loadAsResource(1L);
		});

		Mockito.verify(publicationRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	void testLoadAsResourceWithIOException() {
		PublicationEntity publicationEntity = new PublicationEntity();
		publicationEntity.setFilePath("/tmp/test-uploads/testFile.pdf");
		Mockito.when(publicationRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(publicationEntity));

		assertThrows(StorageFileNotFoundException.class, () -> {
			publicationService.loadAsResource(1L);
		});

		Mockito.verify(publicationRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	void testNameDuplicateHandling() throws IOException, UploadFileException {
		File testFile = new File("/tmp/test-uploads/testFile.pdf");
		testFile.createNewFile();

		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getOriginalFilename()).thenReturn("testFile.pdf");
		Mockito.when(multipartFile.getSize()).thenReturn(100L);
		Mockito.doAnswer(invocation -> {
			File dest = invocation.getArgument(0);
			dest.createNewFile();
			return null;
		}).when(multipartFile).transferTo(Mockito.any(File.class));

		File testFileNew = new File("/tmp/test-uploads/testFile-1.pdf");


		ModuleEntity moduleEntity = new ModuleEntity();
		Mockito.when(moduleService.findById(Mockito.anyLong())).thenReturn(Optional.of(moduleEntity));

		UserEntity userEntity = new UserEntity();
		Mockito.when(userService.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));

		Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

		Mockito.when(inscriptionService.hasWriterAccess(Mockito.any(), Mockito.any())).thenReturn(true);

		publicationService.saveMultipartFile(multipartFile, "testFile", "testDescription", 1L, "test@test.com", FileType.CM);

		Mockito.verify(publicationRepository, Mockito.times(1)).save(Mockito.any(PublicationEntity.class));

		assertTrue(testFileNew.exists());


	}
}
