package swd.billiardshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.dto.response.ChatRoomResponse;
import swd.billiardshop.dto.response.MessageResponse;
import swd.billiardshop.entity.ChatRoom;
import swd.billiardshop.entity.Message;
import swd.billiardshop.entity.User;
import swd.billiardshop.enums.ChatRoomStatus;
import swd.billiardshop.enums.MessageType;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.repository.ChatRoomRepository;
import swd.billiardshop.repository.MessageRepository;
import swd.billiardshop.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Customer tạo hoặc lấy room chat của mình
     */
    @Transactional
    public ChatRoomResponse getOrCreateCustomerRoom(Integer customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tìm room active của customer
        ChatRoom room = chatRoomRepository.findByCustomerAndStatus(customer, ChatRoomStatus.ACTIVE)
                .orElseGet(() -> {
                    // Tạo room mới nếu chưa có
                    ChatRoom newRoom = ChatRoom.builder()
                            .customer(customer)
                            .status(ChatRoomStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return mapToRoomResponse(room, customer);
    }

    /**
     * Staff lấy danh sách tất cả room (đã assign + chưa assign)
     */
    public List<ChatRoomResponse> getStaffRooms(Integer staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy room đã assign cho staff
        List<ChatRoom> assignedRooms = chatRoomRepository
                .findByStaffAndStatusOrderByLastMessageAtDesc(staff, ChatRoomStatus.ACTIVE);

        // Lấy room chưa assign (pending)
        List<ChatRoom> unassignedRooms = chatRoomRepository
                .findByStaffIsNullAndStatusOrderByCreatedAtDesc(ChatRoomStatus.ACTIVE);

        // Merge và sort theo lastMessageAt
        List<ChatRoom> allRooms = new java.util.ArrayList<>(assignedRooms);
        allRooms.addAll(unassignedRooms);
        allRooms.sort((r1, r2) -> {
            LocalDateTime t1 = r1.getLastMessageAt() != null ? r1.getLastMessageAt() : r1.getCreatedAt();
            LocalDateTime t2 = r2.getLastMessageAt() != null ? r2.getLastMessageAt() : r2.getCreatedAt();
            return t2.compareTo(t1);
        });

        return allRooms.stream()
                .map(room -> mapToRoomResponse(room, staff))
                .collect(Collectors.toList());
    }

    /**
     * Staff assign room cho mình
     */
    @Transactional
    public ChatRoomResponse assignRoomToStaff(Integer roomId, Integer staffId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Room not found"));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        room.setStaff(staff);
        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        return mapToRoomResponse(room, staff);
    }

    /**
     * Gửi tin nhắn
     */
    @Transactional
    public MessageResponse sendMessage(Integer roomId, Integer senderId, String content, MessageType type) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Room not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .messageType(type != null ? type : MessageType.TEXT)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        message = messageRepository.save(message);

        // Cập nhật lastMessageAt của room
        room.setLastMessageAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        return mapToMessageResponse(message);
    }

    /**
     * Lấy lịch sử chat
     */
    public Page<MessageResponse> getChatHistory(Integer roomId, Integer userId, int page, int size) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra quyền truy cập
        if (!room.getCustomer().getUserId().equals(userId) &&
                (room.getStaff() == null || !room.getStaff().getUserId().equals(userId))) {
            throw new AppException(ErrorCode.FORBIDDEN, "Access denied");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByRoomOrderByCreatedAtDesc(room, pageable);

        return messages.map(this::mapToMessageResponse);
    }

    @Transactional
    public void markMessagesAsRead(Integer roomId, Integer userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Update tất cả tin nhắn chưa đọc từ người khác
        int updatedCount = messageRepository.markAllAsReadNative(roomId, userId);

        log.info("✅ Marked {} messages as read in room {} for user {}", updatedCount, roomId, userId);
    }

    /**
     * Đếm tin nhắn chưa đọc trong room
     */
    public Long countUnreadMessages(Integer roomId, Integer userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return messageRepository.countByRoomAndSenderNotAndIsReadFalse(room, user);
    }

    /**
     * Đếm tổng số room chưa đọc của staff
     */
    public Long countUnreadRoomsForStaff(Integer staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return chatRoomRepository.countUnreadRoomsByStaff(staff);
    }

    // Helper methods
    private ChatRoomResponse mapToRoomResponse(ChatRoom room, User currentUser) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setRoomId(room.getRoomId());
        response.setStatus(room.getStatus().name());
        response.setCreatedAt(room.getCreatedAt());
        response.setLastMessageAt(room.getLastMessageAt());

        // Customer info
        response.setCustomerId(room.getCustomer().getUserId());
        response.setCustomerName(room.getCustomer().getFullName());
        response.setCustomerAvatar(room.getCustomer().getAvatarUrl());

        // Staff info
        if (room.getStaff() != null) {
            response.setStaffId(room.getStaff().getUserId());
            response.setStaffName(room.getStaff().getFullName());
            response.setStaffAvatar(room.getStaff().getAvatarUrl());
        }

        // Last message
        messageRepository.findFirstByRoomOrderByCreatedAtDesc(room)
                .ifPresent(msg -> {
                    response.setLastMessage(msg.getContent());
                    response.setLastMessageTime(msg.getCreatedAt());
                    response.setLastMessageSender(msg.getSender().getFullName());
                });

        // Unread count
        Long unreadCount = messageRepository.countByRoomAndSenderNotAndIsReadFalse(room, currentUser);
        response.setUnreadCount(unreadCount.intValue());

        return response;
    }

    private MessageResponse mapToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setMessageId(message.getMessageId());
        response.setRoomId(message.getRoom().getRoomId());
        response.setSenderId(message.getSender().getUserId());
        response.setSenderName(message.getSender().getFullName());
        response.setSenderAvatar(message.getSender().getAvatarUrl());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType().name());
        response.setAttachmentUrl(message.getAttachmentUrl());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        response.setReadAt(message.getReadAt());
        return response;
    }
}