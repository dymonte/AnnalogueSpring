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

    @Autowired
    private IInscriptionService inscriptionService;

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
				throw new StorageFileNotFoundException();
			}
			Path file = Path.of(stringPath);
			Resource resource = new FileSystemResource(file);
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException();

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException();
		}

	}

	public void addPublication(PublicationEntity publication) {
		publicationRepository.save(publication);
	}

	public void saveMultipartFile(MultipartFile file, String name, String description, Long moduleId, String authorEmail, FileType fileType) throws UploadFileException {

		if (file.getSize() > 10 * 1024 * 1024)
			throw new UploadFileException("error.upload.file.size");

		//find the author and module
		UserEntity author = userService.findByEmail(authorEmail).orElseThrow(() -> new UploadFileException("error.upload.author.notfound"));

		ModuleEntity module = moduleService.findById(moduleId).orElseThrow(() -> new UploadFileException("error.upload.module.notfound"));

		if(!canUpload(author, module))
			throw new UploadFileException("error.upload.forbidden");

		File fullPath = getFullPath(file);
		try {
			file.transferTo(fullPath);
		}
		catch (IOException e) {
			throw new UploadFileException("error.upload.failed");
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

	private @NonNull File getFullPath(MultipartFile file) throws UploadFileException {


		//save the file in the storage
		File uploadDirectory = new File(uploadDir);

		if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
			throw new UploadFileException("error.upload.cannotcreatedir");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null) {
			throw new UploadFileException("error.upload.filename.missing");
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

	private boolean canUpload(UserEntity user, ModuleEntity module) {
		return inscriptionService.hasWriterAccess(user, module);
	}
}
