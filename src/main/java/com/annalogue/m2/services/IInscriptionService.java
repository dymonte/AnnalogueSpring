package com.annalogue.m2.services;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.UserEntity;

import java.util.List;
import java.util.Optional;

public interface IInscriptionService {

    public Optional<InscriptionEntity> findById(Long id);
    public List<InscriptionEntity> findAll();
    public List<InscriptionEntity>  findByModuleId(Long id);
    public List<InscriptionEntity>   findByUserId(Long id);
    public List<InscriptionEntity> findByEditorFalse();
    public List<InscriptionEntity> findByEditorTrue();
    public void deleteById(Long id);
    public boolean existsById(Long id);
    public List<InscriptionEntity> findInscriptions(UserEntity currentUser);
    public boolean toggleEditor(Long inscriptionId);
    public InscriptionEntity save(UserEntity currentUser,String userEmail,Long moduleId);
}
