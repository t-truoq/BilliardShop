package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swd.billiardshop.entity.Shipment;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // New methods for better performance
    Optional<Shipment> findByOrderOrderId(Integer orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}