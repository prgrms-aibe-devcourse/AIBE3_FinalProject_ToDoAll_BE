package com.server.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 수신자 ID

    private String title;
    private String message;

    private boolean readFlag = false; // 읽음 여부

    private LocalDateTime createdAt = LocalDateTime.now();

    public void markRead() {
        this.readFlag = true;
    }


    public static Notification of(Long userId, String title, String message) {
        Notification n = new Notification();
        n.userId = userId;
        n.title = title;
        n.message = message;
        n.readFlag = false;
        return n;
    }
}