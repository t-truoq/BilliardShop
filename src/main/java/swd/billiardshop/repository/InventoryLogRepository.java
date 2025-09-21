package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.InventoryLog;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {
}
