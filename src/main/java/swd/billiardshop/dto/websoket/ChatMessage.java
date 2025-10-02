package swd.billiardshop.dto.websoket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private Integer userId;        // ID người gửi - QUAN TRỌNG
    private String from;           // Tên người gửi
    private String to;             // Tên người nhận
    private String content;        // Nội dung tin nhắn
    private String type;           // CHAT, JOIN, LEAVE, TYPING, SYSTEM
    private Integer roomId;        // ID của room
    private LocalDateTime timestamp;
    private String attachmentUrl;  // URL file đính kèm

    // Lombok @Data sẽ tự generate các method getter/setter
    // getUserId(), setUserId(), etc.
}