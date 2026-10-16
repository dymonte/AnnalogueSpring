package com.annalogue.m2.services;

import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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
}