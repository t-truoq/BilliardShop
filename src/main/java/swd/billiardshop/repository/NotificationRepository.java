package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
