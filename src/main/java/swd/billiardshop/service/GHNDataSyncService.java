package swd.billiardshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import swd.billiardshop.dto.ghn.GHNProvince;
import swd.billiardshop.dto.ghn.GHNDistrict;
import swd.billiardshop.dto.ghn.GHNWard;
import swd.billiardshop.entity.LocationMapping;
import swd.billiardshop.repository.LocationMappingRepository;

import java.util.List;

@Service
public class GHNDataSyncService {
    private static final Logger logger = LoggerFactory.getLogger(GHNDataSyncService.class);

    @Value("${ghn.sync-enabled:false}")
    private boolean syncEnabled;

    @Autowired
    private GHNClientService ghnClient;

    @Autowired
    private LocationMappingRepository mappingRepo;

    private volatile boolean syncing = false;

    @PostConstruct
    public void init() {
        if (!syncEnabled) {
            logger.info("GHN data sync disabled (ghn.sync-enabled=false)");
            return;
        }

        // run sync asynchronously so startup is not blocked
        syncAllLocationDataAsync();
    }

    @Async
    public void syncAllLocationDataAsync() {
        try {
            syncing = true;
            if (mappingRepo.count() > 0) {
                logger.info("GHN data already present ({} records). Skipping initial sync.", mappingRepo.count());
                syncing = false;
                return;
            }

            logger.info("Starting GHN master-data sync (this may take several minutes)...");
            List<GHNProvince> provinces = ghnClient.getProvinces();
            if (provinces == null || provinces.isEmpty()) {
                logger.warn("No provinces received from GHN API");
                syncing = false;
                return;
            }

            for (GHNProvince p : provinces) {
                logger.info("Syncing province {}", p.getProvinceName());
                List<GHNDistrict> districts = ghnClient.getDistricts(p.getProvinceId());
                if (districts == null) continue;
                for (GHNDistrict d : districts) {
                    List<GHNWard> wards = ghnClient.getWards(d.getDistrictId());
                    if (wards == null) continue;
                    for (GHNWard w : wards) {
                        try {
                            LocationMapping m = LocationMapping.builder()
                                    .province(p.getProvinceName())
                                    .city(p.getProvinceName())
                                    .district(d.getDistrictName())
                                    .ward(w.getWardName())
                                    .ghnDistrictId(d.getDistrictId())
                                    .ghnWardCode(w.getWardCode())
                                    .build();
                            mappingRepo.save(m);
                        } catch (Exception ex) {
                            logger.debug("Failed to save mapping for {} / {} / {}", p.getProvinceName(), d.getDistrictName(), w.getWardName(), ex);
                        }
                    }
                }
            }

            logger.info("GHN master-data sync completed. Total records: {}", mappingRepo.count());
            syncing = false;
        } catch (Exception ex) {
            syncing = false;
            logger.warn("GHNDataSyncService sync failed", ex);
        }
    }

    public boolean isSyncing() {
        return syncing;
    }
}
