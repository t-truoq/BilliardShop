package swd.billiardshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.dto.response.ShipmentResponse;
import swd.billiardshop.entity.Shipment;
import swd.billiardshop.service.ShipmentService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @GetMapping("/{trackingNumber}/track")
    public ResponseEntity<ShipmentResponse> trackShipment(@PathVariable String trackingNumber) {
        ShipmentResponse shipment = shipmentService.updateShipmentFromGHN(trackingNumber);
        if (shipment != null) {
            return ResponseEntity.ok(shipment);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderId(@PathVariable Integer orderId) {
        ShipmentResponse shipment = shipmentService.getShipmentByOrderId(orderId);
        if (shipment != null) {
            return ResponseEntity.ok(shipment);
        }
        return ResponseEntity.notFound().build();
    }
}