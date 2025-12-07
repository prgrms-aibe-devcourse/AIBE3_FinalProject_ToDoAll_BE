package com.server.notification.repository;

import com.server.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findBySentFalseAndScheduledAtBefore(LocalDateTime now);

    List<Notification> findByReadTrueAndCreatedAtBeforeAndDeletedAtIsNull(LocalDateTime threshold);

    void deleteByUserId(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllAsRead(Long userId);

}