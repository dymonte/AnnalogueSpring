package com.annalogue.m2.repositories;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long> {


    List<InscriptionEntity> findByUserId(Long userId);


    List<InscriptionEntity> findByModuleId(Long moduleId);


    List<InscriptionEntity> findByInscriptionType(
            InscriptionType inscriptionType
    );

    List<InscriptionEntity> findByEditeurTrue();

    List<InscriptionEntity> findByEditeurFalse();


}
