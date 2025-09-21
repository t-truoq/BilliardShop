package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import swd.billiardshop.enums.Carrier;
import swd.billiardshop.enums.ShipmentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shipmentId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(length = 100, unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Carrier carrier = Carrier.GHN;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(precision = 15, scale = 2)
    private BigDecimal shippingCost;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime shippedAt;

    @Column(columnDefinition = "TEXT")
    private String carrierResponse;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
