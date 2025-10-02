package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import swd.billiardshop.enums.MessageType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 255)
    private String attachmentUrl;

    @Column(length = 255)
    private String attachmentName;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}