package swd.billiardshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Integer messageId;
    private Integer roomId;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String messageType;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
