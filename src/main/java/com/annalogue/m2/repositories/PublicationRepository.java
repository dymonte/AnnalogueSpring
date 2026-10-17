package com.annalogue.m2.repositories;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.parser.Entity;
import java.util.Collection;
import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<PublicationEntity, Long> {

	// Find by module
	List<PublicationEntity> findByModule(ModuleEntity module);

	// Find by author
	List<PublicationEntity> findByAuthor(UserEntity author);

	@Query("SELECT DISTINCT p FROM PublicationEntity p JOIN p.module m JOIN InscriptionEntity i ON i.module = m WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND i.user.id = :userId")
	List<PublicationEntity> findPublicationsByQueryAndUserId(@Param("query") String query, @Param("userId") Long userId);
}
