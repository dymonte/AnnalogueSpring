package com.annalogue.m2.services;

import com.annalogue.m2.models.InscriptionEntity;
import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.Role;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InscriptionService inscriptionService;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, InscriptionService inscriptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.inscriptionService = inscriptionService;
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // several users can have the same username: use the email or the id to find one user
    public List<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    public List<UserEntity> findByRole(Role role) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
        // all users, sorted by username
    public List<UserEntity> findAll() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public void delete(UserEntity user) {
        userRepository.delete(user);
        // send the DELETE now: if the user is still linked to another table,
        // the database refuses it here, where HandleUserService can catch it
        userRepository.flush();
    }

    public List<InscriptionEntity> findInscriptions(
            UserEntity currentUser) {

        List<InscriptionEntity> inscriptions = new ArrayList<>();

        List<ModuleEntity> responsibleModules =
                currentUser.getResponsibleModules();

        for (ModuleEntity module : responsibleModules) {

            inscriptions.addAll(
                    inscriptionService.findByModuleId(module.getId())
            );
        }

        return inscriptions;
    }

}