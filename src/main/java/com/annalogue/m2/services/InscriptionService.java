
package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.*;
import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.InscriptionType;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.ModuleRepository;
import com.annalogue.m2.repositories.PublicationRepository;
import com.annalogue.m2.repositories.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class InscriptionService implements IInscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final PublicationRepository publicationRepository;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public InscriptionService(
            InscriptionRepository inscriptionRepository,
            ModuleRepository moduleRepository,
            UserRepository userRepository,
            PublicationRepository publicationRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.publicationRepository = publicationRepository;
    }

    public Optional<InscriptionEntity> findById(Long id) {
        return inscriptionRepository.findById(id);
    }

    public List<InscriptionEntity> findAll() {
        return inscriptionRepository.findAll();
    }

    public List<InscriptionEntity>  findByModuleId(Long id) {
        return inscriptionRepository.findByModuleId(id);
    }

    public List<InscriptionEntity>   findByUserId(Long id) {
        return inscriptionRepository.findByUserId(id);
    }

    public List<InscriptionEntity> findByEditeurFalse(){
        return inscriptionRepository.findByEditeurFalse();
    }

    public List<InscriptionEntity> findByEditeurTrue(){
        return inscriptionRepository.findByEditeurTrue();
    }


    @Transactional
    public InscriptionEntity save(
            UserEntity currentUser,
            String userEmail,
            Long moduleId) {

        if (currentUser == null) {
            throw new IllegalArgumentException(
                    "error.inscription.prof.not.found"
            );
        }

        if (moduleId == null) {
            throw new IllegalArgumentException(
                    "error.inscription.module.required"
            );
        }

        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "error.inscription.email.required"
            );
        }

        if(!EMAIL_PATTERN.matcher(userEmail.trim()).matches()){
            throw new IllegalArgumentException(
                    "error.inscription.email.invalid"
            );
        }

        UserEntity member = userRepository.findByEmail(userEmail.trim())
                .orElseThrow(() ->
                        new EmailNotFoundException(
                                "error.inscription.user.not.found"
                        )
                );

        ModuleEntity module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ModuleNotFoundException(
                                "error.inscription.module.not.found"
                        )
                );

        if (module.getResponsible() == null
                || !module.getResponsible().getId()
                .equals(currentUser.getId())) {
            throw new ModuleNotManagedException(
                    "error.inscription.module.not.managed"
            );
        }

        boolean alreadyRegistered =
                inscriptionRepository.findByUserId(member.getId())
                        .stream()
                        .anyMatch(inscription ->
                                inscription.getModule() != null
                                        && inscription.getModule().getId()
                                        .equals(module.getId())
                        );

        if (alreadyRegistered) {
            throw new UserAlreadyEnrolledException(
                    "error.inscription.user.already.registered"
            );
        }

        InscriptionType inscriptionType;

        if (member.getRole() == Role.PROF) {
            inscriptionType = InscriptionType.INTERVENANT;
        } else if (member.getRole() == Role.ETUDIANT) {
            inscriptionType = InscriptionType.ETUDIANT;
        } else {
            throw new IllegalArgumentException(
                    "error.inscription.user.role.invalid"
            );
        }

        InscriptionEntity inscription = new InscriptionEntity();
        inscription.setUser(member);
        inscription.setModule(module);
        inscription.setInscriptionType(inscriptionType);

        return inscriptionRepository.save(inscription);
    }

    public boolean canAccessFile(String email, Long fileId) throws UserNotFoundException, StorageFileNotFoundException {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        ModuleEntity module =  publicationRepository.findById(fileId).orElseThrow(StorageFileNotFoundException::new).getModule();

        return hasReaderAccess(user, module);
    }

    public boolean hasWriterAccess(UserEntity user, ModuleEntity module) {
        InscriptionEntity inscription = inscriptionRepository.findByUserAndModule(user, module).orElse(null);

        return user.getRole() == Role.ADMIN || (inscription != null && inscription.isEditeur());
    }

    public boolean hasReaderAccess(UserEntity user, ModuleEntity module) {
        InscriptionEntity inscription = inscriptionRepository.findByUserAndModule(user, module).orElse(null);
        return user.getRole() == Role.ADMIN || inscription != null; // all inscription are at least reader
    }
}
