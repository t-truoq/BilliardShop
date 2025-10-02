package swd.billiardshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// ChatRoomResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Integer roomId;
    private Integer customerId;
    private String customerName;
    private String customerAvatar;
    private Integer staffId;
    private String staffName;
    private String staffAvatar;
    private String status;
    private String lastMessage;
    private String lastMessageSender;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
}


