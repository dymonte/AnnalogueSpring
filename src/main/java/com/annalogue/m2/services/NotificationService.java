package com.annalogue.m2.services;

import com.annalogue.m2.models.*;
import com.annalogue.m2.repositories.InscriptionRepository;
import com.annalogue.m2.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final InscriptionRepository inscriptionRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               InscriptionRepository inscriptionRepository) {
        this.notificationRepository = notificationRepository;
        this.inscriptionRepository = inscriptionRepository;
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
                .map(user -> new NotificationEntity(user, NotificationType.PUBLICATION_ADDED, module.getName()))
                .toList();

        notificationRepository.saveAll(notifications);
    }
}