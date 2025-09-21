package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
}
