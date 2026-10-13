package com.annalogue.m2.services;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface IInscriptionService {

    public Optional<InscriptionEntity> findById(Long id);
    public List<InscriptionEntity> findAll();
    public List<InscriptionEntity>  findByModuleId(Long id);
    public List<InscriptionEntity>   findByUserId(Long id);
    public List<InscriptionEntity> findByEditeurFalse();
    public List<InscriptionEntity> findByEditeurTrue();
    @Transactional
    public InscriptionEntity save(
            UserEntity currentUser,
            String userEmail,
            Long moduleId);
}
