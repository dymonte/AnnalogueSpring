package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.FileType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.PublicationRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;



@Service
public class PublicationService implements IPublicationService {

	@Autowired
	private PublicationRepository publicationRepository;

	@Autowired
	private IUserService userService;

	@Autowired
	private IModuleService moduleService;

	@Value("${app.upload.dir}")
	private String uploadDir;

	public List<PublicationEntity> findAll() {
		return publicationRepository.findAll();
	}

	public Optional<PublicationEntity> findById(Long id) {
		return publicationRepository.findById(id);
	}

	public List<PublicationEntity> findByAuthor(UserEntity author) {
		return publicationRepository.findByAuthor(author);
	}

	public List<PublicationEntity> findByModule(ModuleEntity module) {
		return publicationRepository.findByModule(module);
	}

	public Resource loadAsResource(Long id) throws StorageFileNotFoundException {
		String stringPath = publicationRepository.findById(id).map(PublicationEntity::getFilePath).orElse(null);
		try {
			if (stringPath == null) {
				throw new StorageFileNotFoundException("Could not read file: publication not found");
			}
			Path file = Path.of(stringPath);
			Resource resource = new FileSystemResource(file);
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException(
					"Could not read file: " + stringPath);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + stringPath);
		}

	}

	public void addPublication(PublicationEntity publication) {
		publicationRepository.save(publication);
	}

	public void saveMultipartFile(MultipartFile file, String name, String description, Long moduleId, String authorEmail, FileType fileType) throws UploadFileException {


		//find the author and module
		UserEntity author = userService.findByEmail(authorEmail).orElseThrow(() -> new UploadFileException("Failed to upload file : author not found"));

		ModuleEntity module = moduleService.findById(moduleId).orElseThrow(() -> new UploadFileException("Failed to upload file : module not found"));

		if(!canUpload(module, author))
			throw new UploadFileException("Failed to upload file : user is not allowed to upload to this module");

		File fullPath = getFullPath(file, module, author);
		try {
			file.transferTo(fullPath);
		}
		catch (IOException e) {
			throw new UploadFileException("Failed to upload file: " + e.getMessage());
		}

		PublicationEntity newPublication = new PublicationEntity(
				name,
				description,
				fileType,
				module,
				author,
				fullPath.toString()
		);
		addPublication(newPublication);
	}

	private @NonNull File getFullPath(MultipartFile file, ModuleEntity module, UserEntity author) throws UploadFileException {


		//save the file in the storage
		File uploadDirectory = new File(uploadDir);

		if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
			throw new UploadFileException("Failed to upload file : could not create upload directory");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null) {
			throw new UploadFileException("Failed to upload file : no file name");
		}
		File fullPath = new File(uploadDirectory, originalFilename);

		// avoid overwriting existing files by adding a number
		int i = 1;
		while (fullPath.exists()) {
			fullPath = new File(uploadDirectory, originalFilename.substring(0, originalFilename.lastIndexOf('.')) + "-" + i + originalFilename.substring(originalFilename.lastIndexOf('.')));
			i++;
		}
		return fullPath;
	}

	private boolean canUpload(ModuleEntity module, UserEntity user) {
		return true; // TODO check with relations
	}
}
