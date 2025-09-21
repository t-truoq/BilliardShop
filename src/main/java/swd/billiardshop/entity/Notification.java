package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import swd.billiardshop.enums.NotificationType;
import swd.billiardshop.enums.NotificationReferenceType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NotificationType type;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotificationReferenceType referenceType;
    private Integer referenceId;

    @Builder.Default
    private Boolean isRead = false;
    private LocalDateTime readAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
