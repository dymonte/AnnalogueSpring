package com.annalogue.m2.services;

import com.annalogue.m2.exceptions.NotificationStateException;
import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.NotificationRepository;
import com.annalogue.m2.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               InscriptionRepository inscriptionRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<NotificationEntity> findForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(userId);
    }

    @Override
    public long countUnseen(Long userId) {
        return notificationRepository.countByRecipientIdAndSeenFalse(userId);
    }

    @Override
    @Transactional
    public int markAllAsSeen(Long userId) {
        return notificationRepository.markAllAsSeen(userId);
    }

    @Override
    public void notifyAccountCreated(UserEntity user) {
        notificationRepository.save(new NotificationEntity(user, NotificationType.ACCOUNT_CREATED, null));
    }

    @Override
    public void notifyEnrollment(UserEntity user, ModuleEntity module) {
        if (!user.isNotificationsEnabled()) {
            return;
        }
        notificationRepository.save(
                new NotificationEntity(user, NotificationType.MODULE_ENROLLMENT, module.getName()));
    }

    @Override
    public void notifyPublicationAdded(PublicationEntity publication) {
        ModuleEntity module = publication.getModule();
        Map<Long, UserEntity> recipients = new LinkedHashMap<>();

        if (module.getResponsible() != null) {
            recipients.put(module.getResponsible().getId(), module.getResponsible());
        }
        for (InscriptionEntity inscription : inscriptionRepository.findByModuleId(module.getId())) {
            recipients.putIfAbsent(inscription.getUser().getId(), inscription.getUser());
        }
        if (publication.getAuthor() != null) {
            recipients.remove(publication.getAuthor().getId());
        }

        List<NotificationEntity> notifications = recipients.values().stream()
                .filter(UserEntity::isNotificationsEnabled)
                .map(user -> new NotificationEntity(user, NotificationType.PUBLICATION_ADDED, module.getName()))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void enableNotifications(Long userId) {
        changeState(userId, true);
    }

    @Override
    @Transactional
    public void disableNotifications(Long userId) {
        changeState(userId, false);
    }

    private void changeState(Long userId, boolean enabled) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotificationStateException("error.notification.user.notfound"));
        if (user.isNotificationsEnabled() == enabled) {
            throw new NotificationStateException(enabled
                    ? "error.notification.already.enabled"
                    : "error.notification.already.disabled");
        }

        user.setNotificationsEnabled(enabled);
        userRepository.save(user);
    }
}