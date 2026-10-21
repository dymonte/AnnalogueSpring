package com.annalogue.m2.services;

import com.annalogue.m2.models.ModuleEntity;
import com.annalogue.m2.models.NotificationEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;

import java.util.List;

public interface INotificationService {

    List<NotificationEntity> findForUser(Long userId);

    long countUnseen(Long userId);

    int markAllAsSeen(Long userId);

    void notifyAccountCreated(UserEntity user);

    void notifyEnrollment(UserEntity user, ModuleEntity module);

    void notifyPublicationAdded(PublicationEntity publication);

    void enableNotifications(Long userId);

    void disableNotifications(Long userId);
}