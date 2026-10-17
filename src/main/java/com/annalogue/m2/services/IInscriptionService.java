package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.*;
import com.annalogue.m2.models.*;
import org.springframework.transaction.annotation.Transactional;

import com.annalogue.m2.dto.GroupEnrollmentResult;

import java.util.List;
import java.util.Optional;

public interface IInscriptionService {

    public Optional<InscriptionEntity> findById(Long id);

    public List<InscriptionEntity> findAll();

    public List<InscriptionEntity> findByModuleId(Long id);

    public List<InscriptionEntity> findByUserId(Long id);

    public List<InscriptionEntity> findByEditorFalse();

    public List<InscriptionEntity> findByEditorTrue();

    @Transactional
    public InscriptionEntity save(UserEntity currentUser, String userEmail, Long moduleId);

    public boolean canAccessFile(String email, Long fileId) throws UserNotFoundException, StorageFileNotFoundException;

    public void deleteById(Long id);

    public void deleteAll();

    public boolean existsById(Long id);

    public boolean hasWriterAccess(UserEntity user, ModuleEntity module);

    public boolean hasReaderAccess(UserEntity user, ModuleEntity module);

    public List<ModuleEntity> getAllModulesEditable(UserEntity user);

    public List<InscriptionEntity> findInscriptions(UserEntity currentUser);

    @Transactional
    public boolean toggleEditor(Long inscriptionId);

    List<ModuleEntity> getModulesAsIntervenant(UserEntity user);

    @Transactional
    public GroupEnrollmentResult saveGroup(UserEntity currentUser, String group, Long moduleId);
}