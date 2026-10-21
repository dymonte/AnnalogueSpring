package com.annalogue.m2.repositories;

import com.annalogue.m2.models.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId);

    long countByRecipientIdAndSeenFalse(Long recipientId);

    @Modifying
    @Query("update NotificationEntity n set n.seen = true where n.recipient.id = :recipientId and n.seen = false")
    int markAllAsSeen(@Param("recipientId") Long recipientId);
}