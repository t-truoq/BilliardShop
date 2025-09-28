package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.LocationMapping;

import java.util.List;
import java.util.Optional;

public interface LocationMappingRepository extends JpaRepository<LocationMapping, Integer> {

    Optional<LocationMapping> findFirstByProvinceIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
            String province, String city, String district, String ward);

    Optional<LocationMapping> findFirstByProvinceIgnoreCaseAndCityIsNullAndDistrictIgnoreCaseAndWardIgnoreCase(
            String province, String district, String ward);

    Optional<LocationMapping> findFirstByProvinceIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
            String province, String district, String ward);

    List<LocationMapping> findByProvinceIgnoreCaseAndDistrictIgnoreCase(
            String province, String district);

}
