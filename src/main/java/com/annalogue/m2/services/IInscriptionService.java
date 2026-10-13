package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.*;
import com.annalogue.m2.models.*;
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
    public InscriptionEntity save(UserEntity currentUser, String userEmail, Long moduleId);

    public boolean canAccessFile(String email, Long fileId) throws UserNotFoundException, StorageFileNotFoundException;

    public boolean hasWriterAccess(UserEntity user, ModuleEntity module);

    public boolean hasReaderAccess(UserEntity user, ModuleEntity module);

    public List<ModuleEntity> getAllModulesEditable(UserEntity user);
}