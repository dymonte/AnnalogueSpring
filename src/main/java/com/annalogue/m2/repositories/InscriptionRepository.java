package com.annalogue.m2.repositories;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long> {


    List<InscriptionEntity> findByUserId(Long userId);


    List<InscriptionEntity> findByModuleId(Long moduleId);


    List<InscriptionEntity> findByInscriptionType(
            InscriptionType inscriptionType
    );

    List<InscriptionEntity> findByEditeurTrue();

    List<InscriptionEntity> findByEditeurFalse();


}
