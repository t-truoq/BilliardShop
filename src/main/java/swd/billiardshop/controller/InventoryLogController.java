package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.InventoryLog;
import swd.billiardshop.service.InventoryLogService;
import java.util.List;

@RestController
@RequestMapping("/inventory-logs")
public class InventoryLogController {
    private final InventoryLogService inventoryLogService;

    public InventoryLogController(InventoryLogService inventoryLogService) {
        this.inventoryLogService = inventoryLogService;
    }

    @GetMapping
    public List<InventoryLog> getAllInventoryLogs() {
        return inventoryLogService.getAllInventoryLogs();
    }
}
