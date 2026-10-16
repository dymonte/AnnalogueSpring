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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InscriptionService implements IInscriptionService{

    private final InscriptionRepository inscriptionRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final PublicationRepository publicationRepository;
    private final INotificationService notificationService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public InscriptionService(
            InscriptionRepository inscriptionRepository,
            ModuleRepository moduleRepository,
            UserRepository userRepository,
            PublicationRepository publicationRepository,
            INotificationService notificationService) {
        this.inscriptionRepository = inscriptionRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.publicationRepository = publicationRepository;
        this.notificationService = notificationService;
    }
    @Override
    public Optional<InscriptionEntity> findById(Long id) {
        return inscriptionRepository.findById(id);
    }
    @Override
    public List<InscriptionEntity> findAll() {
        return inscriptionRepository.findAll();
    }
    @Override
    public List<InscriptionEntity>  findByModuleId(Long id) {
        return inscriptionRepository.findByModuleId(id);
    }
    @Override
    public List<InscriptionEntity>   findByUserId(Long id) {
        return inscriptionRepository.findByUserId(id);
    }

    public List<InscriptionEntity> findByEditorFalse(){
        return inscriptionRepository.findByEditorFalse();
    }

    public List<InscriptionEntity> findByEditorTrue(){
        return inscriptionRepository.findByEditorTrue();
    }
    @Override
    public void deleteById(Long id){inscriptionRepository.deleteById(id); }
    @Override
    public boolean existsById(Long id){return inscriptionRepository.existsById(id);}
    @Override
    public List<InscriptionEntity> findInscriptions(UserEntity currentUser) {

        List<InscriptionEntity> inscriptions = new ArrayList<>();

        List<ModuleEntity> responsibleModules =
                currentUser.getResponsibleModules();

        for (ModuleEntity module : responsibleModules) {

            inscriptions.addAll(
                    this.findByModuleId(module.getId())
            );
        }

        return inscriptions;
    }
    @Override
    @Transactional
    public boolean toggleEditor(Long inscriptionId) {
        InscriptionEntity inscription = inscriptionRepository
                .findById(inscriptionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "error.inscription.not.found"
                        )
                );

        boolean isResponsible = inscription.getUser().getId()
                .equals(inscription.getModule().getResponsible().getId());

        if (isResponsible && inscription.isEditor()) {
            throw new IllegalArgumentException(
                    "error.inscription.responsible.cannot.remove.editor"
            );
        }

        inscription.setEditor(!inscription.isEditor());
        return inscription.isEditor();
    }
    @Override
    @Transactional
    public InscriptionEntity save(UserEntity currentUser,String userEmail,Long moduleId) {

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

        // a Responsable Module is a teacher too: intervenant in the modules of the others
        if (member.getRole() == Role.PROF || member.getRole() == Role.RESPONSABLE) {
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
        inscription.setEditor(member.getId().equals(module.getResponsible().getId()));

        InscriptionEntity saved = inscriptionRepository.save(inscription);

        notificationService.notifyEnrollment(member, module);

        return saved;
    }

    public boolean canAccessFile(String email, Long fileId) throws UserNotFoundException, StorageFileNotFoundException {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        ModuleEntity module =  publicationRepository.findById(fileId).orElseThrow(StorageFileNotFoundException::new).getModule();

        return hasReaderAccess(user, module);
    }

    public boolean hasWriterAccess(UserEntity user, ModuleEntity module) {
        InscriptionEntity inscription = inscriptionRepository.findByUserAndModule(user, module).orElse(null);

        return user.getRole() == Role.ADMIN || (inscription != null && inscription.isEditor());
    }

    public boolean hasReaderAccess(UserEntity user, ModuleEntity module) {
        InscriptionEntity inscription = inscriptionRepository.findByUserAndModule(user, module).orElse(null);
        return user.getRole() == Role.ADMIN || inscription != null; // all inscription are at least reader
    }

    public List<ModuleEntity> getAllModulesEditable(UserEntity user) {
        System.out.println("DEBUG getAllModulesEditable: User id = " + user.getId() + ", role = " + user.getRole());

        if (user.getRole() == Role.ADMIN) {
            System.out.println("DEBUG getAllModulesEditable: User is ADMIN, returning all modules");
            return moduleRepository.findAll();
        }

        return inscriptionRepository.findByUserId(user.getId()).stream()
                .filter(InscriptionEntity::isEditor)
                .map(InscriptionEntity::getModule)
                .collect(Collectors.toList());
    }
}