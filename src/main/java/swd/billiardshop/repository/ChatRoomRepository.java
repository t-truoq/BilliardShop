package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.billiardshop.entity.ChatRoom;
import swd.billiardshop.entity.User;
import swd.billiardshop.enums.ChatRoomStatus;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    // Tìm room giữa customer và staff (có thể chưa assign staff)
    Optional<ChatRoom> findByCustomerAndStaffAndStatus(User customer, User staff, ChatRoomStatus status);

    // Tìm room của customer (chưa assign staff hoặc đã assign)
    Optional<ChatRoom> findByCustomerAndStatus(User customer, ChatRoomStatus status);

    // Lấy tất cả room của một staff
    List<ChatRoom> findByStaffAndStatusOrderByLastMessageAtDesc(User staff, ChatRoomStatus status);

    // Lấy tất cả room đang chờ (chưa assign staff)
    List<ChatRoom> findByStaffIsNullAndStatusOrderByCreatedAtDesc(ChatRoomStatus status);

    // Đếm số room chưa assign
    Long countByStaffIsNullAndStatus(ChatRoomStatus status);

    // Đếm số room của staff có tin nhắn chưa đọc
    @Query("SELECT COUNT(DISTINCT m.room) FROM Message m " +
            "WHERE m.room.staff = :staff " +
            "AND m.sender != :staff " +
            "AND m.isRead = false")
    Long countUnreadRoomsByStaff(@Param("staff") User staff);
}
