package swd.billiardshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.entity.ChatRoom;
import swd.billiardshop.entity.Message;
import swd.billiardshop.entity.User;

import java.util.List;
import java.util.Optional;

// MessageRepository
public interface MessageRepository extends JpaRepository<Message, Integer> {



    Page<Message> findByRoomOrderByCreatedAtDesc(ChatRoom room, Pageable pageable);

    // Lấy tin nhắn mới nhất của room
    Optional<Message> findFirstByRoomOrderByCreatedAtDesc(ChatRoom room);

    // Đếm tin nhắn chưa đọc trong room (từ phía customer)
    Long countByRoomAndSenderNotAndIsReadFalse(ChatRoom room, User sender);

    // Đánh dấu tất cả tin nhắn trong room là đã đọc
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
            "WHERE m.room = :room AND m.sender != :reader AND m.isRead = false")
    void markAllAsRead(@Param("room") ChatRoom room, @Param("reader") User reader);

    // Lấy danh sách tin nhắn chưa đọc
    List<Message> findByRoomAndIsReadFalseAndSenderNot(ChatRoom room, User sender);

    @Modifying
    @Transactional
    @Query(value = "UPDATE messages SET is_read = 1, read_at = NOW() " +
            "WHERE room_id = :roomId AND sender_id != :userId AND is_read = 0",
            nativeQuery = true)
    int markAllAsReadNative(@Param("roomId") Integer roomId, @Param("userId") Integer userId);
}