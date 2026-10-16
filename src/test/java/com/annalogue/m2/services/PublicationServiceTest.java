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
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Mock
    private IFilesService filesService;

	@InjectMocks
	private PublicationService publicationService;
	private AutoCloseable mocks;

	@BeforeEach
	public void setUp() throws Exception {
		mocks = MockitoAnnotations.openMocks(this);

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

		File mockFile = new File("/tmp/test-uploads/testFile.pdf");
		Mockito.when(filesService.saveMultipartFile(Mockito.any())).thenReturn(mockFile);

		publicationService.saveMultipartFile(multipartFile, "testFile", "testDescription", 1L, "test@test.com", FileType.CM);

		Mockito.verify(publicationRepository, Mockito.times(1)).save(Mockito.any(PublicationEntity.class));
	}

	@Test
	void testLoadAsResourceSuccessfully() throws StorageFileNotFoundException {
		PublicationEntity publicationEntity = new PublicationEntity();
		publicationEntity.setFilePath("/tmp/test-uploads/testFile.pdf");
		Resource mockResource = mock(Resource.class);

		when(publicationRepository.findById(1L)).thenReturn(Optional.of(publicationEntity));
		when(filesService.loadAsResource("/tmp/test-uploads/testFile.pdf")).thenReturn(mockResource);

		Resource result = publicationService.loadAsResource(1L);

		assertNotNull(result);
		verify(publicationRepository).findById(1L);
		verify(filesService).loadAsResource("/tmp/test-uploads/testFile.pdf");
	}

	@Test
	void testLoadAsResourceWithNullPath() throws StorageFileNotFoundException {
		PublicationEntity publicationEntity = new PublicationEntity();
		publicationEntity.setFilePath(null);

		when(publicationRepository.findById(1L)).thenReturn(Optional.of(publicationEntity));

		assertThrows(StorageFileNotFoundException.class, () -> publicationService.loadAsResource(1L));

		verify(publicationRepository).findById(1L);
		verify(filesService, never()).loadAsResource(any());
	}

	@Test
	void testLoadAsResourceWithNonExistentPublication() throws StorageFileNotFoundException {
		when(publicationRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(StorageFileNotFoundException.class, () -> publicationService.loadAsResource(1L));

		verify(publicationRepository).findById(1L);
		verify(filesService, never()).loadAsResource(any());
	}

	@Test
	void testNameDuplicateHandling() throws IOException, UploadFileException {
		File testFile = new File("/tmp/test-uploads/testFile.pdf");
		testFile.createNewFile();

		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.when(multipartFile.getOriginalFilename()).thenReturn("testFile.pdf");
		Mockito.when(multipartFile.getSize()).thenReturn(100L);

		File testFileNew = new File("/tmp/test-uploads/testFile-1.pdf");
		Mockito.when(filesService.saveMultipartFile(Mockito.any())).thenReturn(testFileNew);

		ModuleEntity moduleEntity = new ModuleEntity();
		Mockito.when(moduleService.findById(Mockito.anyLong())).thenReturn(Optional.of(moduleEntity));

		UserEntity userEntity = new UserEntity();
		Mockito.when(userService.findByEmail(Mockito.anyString())).thenReturn(Optional.of(userEntity));

		Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encodedPassword");

		Mockito.when(inscriptionService.hasWriterAccess(Mockito.any(), Mockito.any())).thenReturn(true);

		publicationService.saveMultipartFile(multipartFile, "testFile", "testDescription", 1L, "test@test.com", FileType.CM);

		Mockito.verify(publicationRepository, Mockito.times(1)).save(Mockito.any(PublicationEntity.class));

		Mockito.verify(filesService, Mockito.times(1)).saveMultipartFile(Mockito.any());

	}
}
