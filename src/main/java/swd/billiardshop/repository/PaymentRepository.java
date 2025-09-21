package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
