package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.ChatRoom;
import swd.billiardshop.service.ChatRoomService;
import java.util.List;

@RestController
@RequestMapping("/chat-rooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @GetMapping
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomService.getAllChatRooms();
    }
}
