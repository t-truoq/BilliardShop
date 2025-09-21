package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import swd.billiardshop.enums.InventoryLogType;
import swd.billiardshop.enums.InventoryLogReferenceType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory_logs")
public class InventoryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InventoryLogType type;

    @Column(nullable = false)
    private Integer quantityChange;
    @Column(nullable = false)
    private Integer quantityBefore;
    @Column(nullable = false)
    private Integer quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InventoryLogReferenceType referenceType;
    private Integer referenceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
