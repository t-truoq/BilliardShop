package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.entity.Address;
import swd.billiardshop.entity.User;

import java.util.List;


public interface AddressRepository extends JpaRepository<Address, Integer> {
	List<Address> findByUser(User user);

	boolean existsByUserAndAddressLineAndWardAndDistrictAndCityAndProvince(User user, String addressLine, String ward, String district, String city, String province);

	java.util.Optional<Address> findFirstByUserAndAddressLineAndWardAndDistrictAndCityAndProvince(User user, String addressLine, String ward, String district, String city, String province);

	@Transactional
	@Modifying
	@Query("update Address a set a.isDefault = false where a.user = ?1")
	int clearDefaultForUser(User user);
}
