package swd.billiardshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swd.billiardshop.entity.Address;
import swd.billiardshop.entity.LocationMapping;
import swd.billiardshop.repository.LocationMappingRepository;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;

import java.util.List;
import java.util.Optional;

@Service
public class LocationMappingService {

    private static final Logger log = LoggerFactory.getLogger(LocationMappingService.class);

    @Autowired
    private LocationMappingRepository mappingRepository;

    /**
     * Map local Address -> GHN districtId and wardCode using local DB lookup only.
     * Uses multiple matching strategies (with/without city, partial matches) and
     * throws AppException(ErrorCode.LOCATION_NOT_MAPPED) when no suitable mapping is found.
     */
    public LocationMapping mapAddressToGHN(Address address) {
        if (address == null) return null;

        String province = normalizeProvince(address.getProvince());
        String city = normalizeCity(address.getCity());
        String district = normalizeDistrict(address.getDistrict());
        String ward = normalizeWard(address.getWard());

        log.info("Searching with normalized: province='{}', city='{}', district='{}', ward='{}'",
                province, city, district, ward);

        // Strategy 1: Tìm với đầy đủ thông tin (province, city, district, ward)
        Optional<LocationMapping> found = mappingRepository
                .findFirstByProvinceIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
                        province, city, district, ward);

        if (found.isPresent()) {
            log.info("Found mapping with strategy 1 (full match)");
            return found.get();
        }

        // Strategy 2: Tìm không có city (như code cũ)
        found = mappingRepository
                .findFirstByProvinceIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
                        province, district, ward);

        if (found.isPresent()) {
            log.info("Found mapping with strategy 2 (no city)");
            return found.get();
        }

        // Strategy 3: Tìm với city null hoặc empty
        found = mappingRepository
                .findFirstByProvinceIgnoreCaseAndCityIsNullAndDistrictIgnoreCaseAndWardIgnoreCase(
                        province, district, ward);

        if (found.isPresent()) {
            log.info("Found mapping with strategy 3 (null city)");
            return found.get();
        }

        // Strategy 4: Thử với district gốc (chưa normalize)
        String originalDistrict = address.getDistrict();
        if (!originalDistrict.equals(district)) {
            found = mappingRepository
                    .findFirstByProvinceIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
                            province, originalDistrict, ward);

            if (found.isPresent()) {
                log.info("Found mapping with strategy 4 (original district)");
                return found.get();
            }
        }

        // Strategy 5: Thử với ward gốc (chưa normalize)
        String originalWard = address.getWard();
        if (!originalWard.equals(ward)) {
            found = mappingRepository
                    .findFirstByProvinceIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
                            province, district, originalWard);

            if (found.isPresent()) {
                log.info("Found mapping with strategy 5 (original ward)");
                return found.get();
            }
        }

        // Strategy 6: Thử cả district và ward gốc
        if (!originalDistrict.equals(district) && !originalWard.equals(ward)) {
            found = mappingRepository
                    .findFirstByProvinceIgnoreCaseAndDistrictIgnoreCaseAndWardIgnoreCase(
                            province, originalDistrict, originalWard);

            if (found.isPresent()) {
                log.info("Found mapping with strategy 6 (both original)");
                return found.get();
            }
        }

        // Strategy 7: Tìm chỉ với province, district (bỏ qua ward)
        List<LocationMapping> partialMatches = mappingRepository
                .findByProvinceIgnoreCaseAndDistrictIgnoreCase(province, district);

        if (!partialMatches.isEmpty()) {
            log.info("Found mapping with strategy 7 (partial match): {} results", partialMatches.size());
            return partialMatches.get(0);
        }

        // Strategy 8: Tìm với district gốc (partial)
        if (!originalDistrict.equals(district)) {
            partialMatches = mappingRepository
                    .findByProvinceIgnoreCaseAndDistrictIgnoreCase(province, originalDistrict);

            if (!partialMatches.isEmpty()) {
                log.info("Found mapping with strategy 8 (original district partial): {} results", partialMatches.size());
                return partialMatches.get(0);
            }
        }

        log.warn("No mapping found after all strategies for: province='{}', city='{}', district='{}', ward='{}'",
                province, city, district, ward);

        throw new AppException(ErrorCode.INVALID_REQUEST);
    }

    /**
     * Normalize province name by removing common prefixes
     */
    private String normalizeProvince(String province) {
        if (province == null || province.trim().isEmpty()) return null;

        return province.trim()
                // Remove "Thành phố" prefix
                .replaceAll("^(Thành phố |TP\\.|TP )(.+)", "$2")
                // Remove "Tỉnh" prefix
                .replaceAll("^Tỉnh (.+)", "$1")
                // Specific cases
                .replace("Thành phố Hồ Chí Minh", "Hồ Chí Minh")
                .replace("TP Hồ Chí Minh", "Hồ Chí Minh")
                .replace("TP.HCM", "Hồ Chí Minh")
                .replace("Ho Chi Minh City", "Hồ Chí Minh")
                .replace("Hà Nội", "Hà Nội") // Keep as is
                .replace("Thành phố Hà Nội", "Hà Nội")
                .replace("TP Hà Nội", "Hà Nội");
    }

    /**
     * Normalize city name by removing common prefixes
     */
    private String normalizeCity(String city) {
        if (city == null || city.trim().isEmpty()) return null;

        return city.trim()
                // Remove "Thành phố" prefix
                .replaceAll("^(Thành phố |TP\\.|TP )(.+)", "$2")
                // Remove "Quận" prefix (for districts that might be in city field)
                .replaceAll("^Quận (.+)", "$1")
                // Specific cases
                .replace("Thành phố Hồ Chí Minh", "Hồ Chí Minh")
                .replace("TP Hồ Chí Minh", "Hồ Chí Minh")
                .replace("TP.HCM", "Hồ Chí Minh");
    }

    /**
     * Normalize district name by removing common prefixes
     */
    private String normalizeDistrict(String district) {
        if (district == null || district.trim().isEmpty()) return null;

        String normalized = district.trim();

        // Don't normalize if it's already just a number
        if (normalized.matches("^\\d+$")) {
            return normalized;
        }

        return normalized
                // Remove common prefixes but keep original format as backup
                .replaceAll("^(Quận |Huyện |Thành phố |TP\\.|Thị xã )(.+)", "$2")
                // Keep numbers as is for districts like "Quận 1" -> "1"
                .replaceAll("^Quận (\\d+)$", "$1");
    }

    /**
     * Normalize ward name by removing common prefixes
     */
    private String normalizeWard(String ward) {
        if (ward == null || ward.trim().isEmpty()) return null;

        String normalized = ward.trim();

        // Don't normalize if it's already just a number or simple name
        if (normalized.matches("^\\d+$") || !normalized.matches("^(Phường|Xã|Thị trấn|Khu phố|Khu vực)\\s+.+")) {
            return normalized;
        }

        return normalized
                // Remove common prefixes
                .replaceAll("^(Phường |Xã |Thị trấn |Khu phố |Khu vực )(.+)", "$2")
                // Keep numbers as is for wards like "Phường 1" -> "1"
                .replaceAll("^Phường (\\d+)$", "$1");
    }
}
