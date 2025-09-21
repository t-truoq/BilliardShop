package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
}
