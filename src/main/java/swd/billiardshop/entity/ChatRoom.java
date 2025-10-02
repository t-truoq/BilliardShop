package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import swd.billiardshop.enums.ChatRoomStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    private LocalDateTime lastMessageAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}