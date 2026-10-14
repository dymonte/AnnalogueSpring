package com.annalogue.m2.repositories;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long> {


    List<InscriptionEntity> findByUserId(Long userId);


    List<InscriptionEntity> findByModuleId(Long moduleId);


    List<InscriptionEntity> findByInscriptionType(
            InscriptionType inscriptionType
    );

    List<InscriptionEntity> findByEditorTrue();

    List<InscriptionEntity> findByEditorFalse();

    Optional<InscriptionEntity> findByUserAndModule(UserEntity user, ModuleEntity module);


}
