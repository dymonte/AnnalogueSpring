package com.annalogue.m2.repositories;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long> {


    List<InscriptionEntity> findByUserId(Long userId);


    List<InscriptionEntity> findByModuleId(Long moduleId);


    List<InscriptionEntity> findByInscriptionType(
            InscriptionType inscriptionType
    );

    List<InscriptionEntity> findByEditeurTrue();

    List<InscriptionEntity> findByEditeurFalse();

    Optional<InscriptionEntity> findByUserAndModule(UserEntity user, ModuleEntity module);


}
