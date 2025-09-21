package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.Shipment;
import swd.billiardshop.service.ShipmentService;
import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<Shipment> getAllShipments() {
        return shipmentService.getAllShipments();
    }
}
