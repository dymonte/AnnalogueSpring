package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.StorageFileNotFoundException;
import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.FileType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IPublicationService {
	List<PublicationEntity> findAll();

	Optional<PublicationEntity> findById(Long id);

	List<PublicationEntity> findByAuthor(UserEntity author);

	List<PublicationEntity> findByModule(ModuleEntity module);

	void addPublication(PublicationEntity publication);

	Resource loadAsResource(Long id) throws StorageFileNotFoundException;

	void saveMultipartFile(MultipartFile file, String name, String description, Long moduleId, String authorEmail, FileType fileType) throws UploadFileException;

	Optional<PublicationEntity> findByName(String name);
}
