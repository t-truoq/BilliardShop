package swd.billiardshop.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import swd.billiardshop.service.LocationMappingService;
import swd.billiardshop.repository.AddressRepository;
import swd.billiardshop.entity.Address;

@Component
public class LocationMappingSyncRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(LocationMappingSyncRunner.class);

    @Autowired
    private LocationMappingService mappingService;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            logger.info("LocationMappingSyncRunner: scanning addresses to pre-populate GHN mappings...");
            for (Address a : addressRepository.findAll()) {
                try {
                    if (a == null) continue;
                    mappingService.mapAddressToGHN(a);
                } catch (Exception ex) {
                    logger.debug("Failed to map address id={} : {}", a != null ? a.getAddressId() : null, ex.getMessage());
                }
            }
            logger.info("LocationMappingSyncRunner: mapping sync completed.");
        } catch (Exception e) {
            logger.warn("LocationMappingSyncRunner failed", e);
        }
    }
}
