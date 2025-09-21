package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {
}
