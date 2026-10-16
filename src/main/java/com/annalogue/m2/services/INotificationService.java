package com.annalogue.m2.services;

import com.annalogue.m2.models.NotificationEntity;

import java.util.List;

public interface INotificationService {

    List<NotificationEntity> findForUser(Long userId);

    long countUnseen(Long userId);

    int markAllAsSeen(Long userId);
}