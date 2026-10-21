package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.FileType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    @Lazy
    private IInscriptionService inscriptionService;

    @Autowired
    private IFilesService filesService;

    @Autowired
    private INotificationService notificationService;


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
		String stringPath = publicationRepository.findById(id).map(PublicationEntity::getFilePath).orElseThrow(StorageFileNotFoundException::new);

        return filesService.loadAsResource(stringPath);
	}

	public void addPublication(PublicationEntity publication) {
		publicationRepository.save(publication);
	}

	public void saveMultipartFile(MultipartFile file, String name, String description, Long moduleId, String authorEmail, FileType fileType) throws UploadFileException {

		//find the author and module
		UserEntity author = userService.findByEmail(authorEmail).orElseThrow(() -> new UploadFileException("error.upload.author.notfound"));

		ModuleEntity module = moduleService.findById(moduleId).orElseThrow(() -> new UploadFileException("error.upload.module.notfound"));

		if(!canUpload(author, module))
			throw new UploadFileException("error.upload.forbidden");

		File fullPath = filesService.saveMultipartFile(file);



		PublicationEntity newPublication = new PublicationEntity(
				name,
				description,
				fileType,
				module,
				author,
				fullPath.toString()
		);
		addPublication(newPublication);

		notificationService.notifyPublicationAdded(newPublication);
	}

	@Override
	public Optional<PublicationEntity> findByName(String name) {
		return publicationRepository.findByName(name);
	}

	@Override
	public void deleteAll() {
		inscriptionService.deleteAll();
	}


	private boolean canUpload(UserEntity user, ModuleEntity module) {
		return inscriptionService.hasWriterAccess(user, module);
	}
}