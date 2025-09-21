package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.PromotionUsage;
import swd.billiardshop.service.PromotionUsageService;
import java.util.List;

@RestController
@RequestMapping("/promotion-usages")
public class PromotionUsageController {
    private final PromotionUsageService promotionUsageService;

    public PromotionUsageController(PromotionUsageService promotionUsageService) {
        this.promotionUsageService = promotionUsageService;
    }

    @GetMapping
    public List<PromotionUsage> getAllPromotionUsages() {
        return promotionUsageService.getAllPromotionUsages();
    }
}
