package com.server.notification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.notification.dto.NotificationPayload;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 수신자 ID

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String message;

    // JSON 형태로 저장되는 추가 데이터
    // 알림의 종류마다 달라지는 데이터를 페이로드에 넣음
    @Column(columnDefinition = "TEXT")
    private String payload;

    private boolean readFlag; // 읽음 여부
    private LocalDateTime createdAt;

    public void markRead() {
        this.readFlag = true;
    }

    public static Notification of(Long userId,NotificationType type, String title, String message, NotificationPayload payload) {
        Notification n = new Notification();
        n.userId = userId;
        n.type = type;
        n.title = title;
        n.message = message;

        try {
            ObjectMapper mapper = new ObjectMapper();
            n.payload = mapper.writeValueAsString(payload); // JSON 문자열로 변환
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert payload to JSON", e);
        }

        n.readFlag = false;
        n.createdAt = LocalDateTime.now();
        return n;
    }
}