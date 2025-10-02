package swd.billiardshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.dto.response.ApiResponse;
import swd.billiardshop.dto.response.ChatRoomResponse;
import swd.billiardshop.dto.response.MessageResponse;
import swd.billiardshop.service.ChatServiceImpl;
import swd.billiardshop.service.UserService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatRestController {

    private final ChatServiceImpl chatService;
    private final UserService userService;

    /**
     * Customer lấy/tạo room chat của mình
     */
    @GetMapping("/user/chat/room")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getCustomerRoom() {
        Integer userId = getCurrentUserId();
        ChatRoomResponse room = chatService.getOrCreateCustomerRoom(userId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * Staff lấy danh sách tất cả room
     */
    @GetMapping("/staff/chat/rooms")
    public ResponseEntity<ApiResponse<Object>> getStaffRooms() {
        Integer staffId = getCurrentUserId();
        var rooms = chatService.getStaffRooms(staffId);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Staff assign room cho mình
     */
    @PostMapping("/staff/chat/rooms/{roomId}/assign")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> assignRoom(@PathVariable Integer roomId) {
        Integer staffId = getCurrentUserId();
        ChatRoomResponse room = chatService.assignRoomToStaff(roomId, staffId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * Lấy lịch sử chat trong room
     */
    @GetMapping("/user/chat/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getChatHistory(
            @PathVariable Integer roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Integer userId = getCurrentUserId();
        Page<MessageResponse> messages = chatService.getChatHistory(roomId, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Đếm tin nhắn chưa đọc
     */
    @GetMapping("/user/chat/rooms/{roomId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Integer roomId) {
        Integer userId = getCurrentUserId();
        Long count = chatService.countUnreadMessages(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Staff: Đếm tổng số room có tin nhắn chưa đọc
     */
    @GetMapping("/staff/chat/unread-rooms-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadRoomsCount() {
        Integer staffId = getCurrentUserId();
        Long count = chatService.countUnreadRoomsForStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // Helper
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return userService.getUserEntityByUsername(auth.getName()).getUserId();
    }
}