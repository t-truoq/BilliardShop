package swd.billiardshop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import swd.billiardshop.dto.websoket.ChatMessage;
import swd.billiardshop.dto.response.MessageResponse;
import swd.billiardshop.enums.MessageType;
import swd.billiardshop.service.ChatServiceImpl;
import swd.billiardshop.service.UserService;
import swd.billiardshop.entity.User;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatServiceImpl chatService;
    private final UserService userService;

    /**
     * User join room - Lưu userId vào session
     */
    @MessageMapping("/chat.join/{roomId}")
    public void joinRoom(
            @DestinationVariable Integer roomId,
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            Integer userId = message.getUserId();

            if (userId != null) {
                // Lưu userId vào session
                headerAccessor.getSessionAttributes().put("userId", userId);
                headerAccessor.getSessionAttributes().put("roomId", roomId);

                log.info("✅ User {} joined room {} (saved to session)", userId, roomId);

                String userName = userService.getUserById(userId).getFullName();

                // Đánh dấu tin nhắn là đã đọc
                chatService.markMessagesAsRead(roomId, userId);

                ChatMessage joinMessage = ChatMessage.builder()
                        .from(userName)
                        .content(userName + " đã tham gia")
                        .type("JOIN")
                        .roomId(roomId)
                        .timestamp(LocalDateTime.now())
                        .build();

                messagingTemplate.convertAndSend("/topic/room." + roomId, joinMessage);
            } else {
                log.error("❌ userId is null in join message");
            }

        } catch (Exception e) {
            log.error("❌ Error joining room", e);
        }
    }

    /**
     * Gửi tin nhắn
     */
    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(
            @DestinationVariable Integer roomId,
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            log.info("📨 Received message in room {}: {}", roomId, message.getContent());

            // Lấy userId từ message hoặc session
            Integer senderId = message.getUserId();

            if (senderId == null) {
                senderId = (Integer) headerAccessor.getSessionAttributes().get("userId");
                log.info("📍 Got userId from session: {}", senderId);
            } else {
                log.info("📍 Got userId from message: {}", senderId);
            }

            if (senderId == null) {
                log.error("❌ Sender ID not found in session or message!");

                // TRY: Lấy từ principal (nếu có authentication)
                Authentication auth = (Authentication) headerAccessor.getUser();
                if (auth != null) {
                    String username = auth.getName();
                    log.info("📍 Trying to get userId from principal username: {}", username);
                    User user = userService.getUserEntityByUsername(username);
                    senderId = user.getUserId();
                    log.info("✅ Got userId from principal: {}", senderId);

                    // Lưu vào session cho lần sau
                    headerAccessor.getSessionAttributes().put("userId", senderId);
                }
            }

            if (senderId == null) {
                log.error("❌ Cannot determine sender ID!");
                return;
            }

            // Lưu message vào DB
            MessageResponse savedMessage = chatService.sendMessage(
                    roomId,
                    senderId,
                    message.getContent(),
                    MessageType.TEXT
            );

            // Chuẩn bị response
            ChatMessage response = ChatMessage.builder()
                    .userId(savedMessage.getSenderId())
                    .from(savedMessage.getSenderName())
                    .content(savedMessage.getContent())
                    .roomId(roomId)
                    .timestamp(savedMessage.getCreatedAt())
                    .type("CHAT")
                    .build();

            // Gửi tin nhắn đến room topic
            messagingTemplate.convertAndSend("/topic/room." + roomId, response);

            // Gửi notification đến staff
            messagingTemplate.convertAndSend("/topic/staff.notifications",
                    new StaffNotification(roomId, "NEW_MESSAGE", savedMessage));

            log.info("✅ Message sent successfully to room {}", roomId);

        } catch (Exception e) {
            log.error("❌ Error sending message", e);
        }
    }

    /**
     * Typing indicator
     */
    @MessageMapping("/chat.typing/{roomId}")
    public void userTyping(
            @DestinationVariable Integer roomId,
            @Payload ChatMessage message) {

        message.setType("TYPING");
        messagingTemplate.convertAndSend("/topic/room." + roomId + ".typing", message);
    }

    @MessageMapping("/chat.read/{roomId}")
    public void markAsRead(
            @DestinationVariable Integer roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");

            if (userId != null) {
                log.info("📖 Marking messages as read for user {} in room {}", userId, roomId);

                chatService.markMessagesAsRead(roomId, userId);

                messagingTemplate.convertAndSend("/topic/room." + roomId + ".read",
                        new ReadReceipt(roomId, userId, LocalDateTime.now()));

                log.info("✅ Messages marked as read");
            } else {
                log.warn("⚠️ Cannot mark as read - userId not in session");
            }
        } catch (Exception e) {
            log.error("❌ Error marking messages as read", e);
        }
    }
}

// Helper classes
@lombok.Data
@lombok.AllArgsConstructor
class StaffNotification {
    private Integer roomId;
    private String type;
    private Object data;
}

@lombok.Data
@lombok.AllArgsConstructor
class ReadReceipt {
    private Integer roomId;
    private Integer userId;
    private LocalDateTime readAt;
}