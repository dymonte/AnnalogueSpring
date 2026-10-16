package com.annalogue.m2.repositories;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<PublicationEntity, Long> {

	// Find by module
	List<PublicationEntity> findByModule(ModuleEntity module);

	// Find by author
	List<PublicationEntity> findByAuthor(UserEntity author);

	List<PublicationEntity> findAllByNameIgnoreCase(String query);

}
