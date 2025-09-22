package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_categories_slug", columnNames = {"slug"}),
    @UniqueConstraint(name = "uk_categories_parent_name", columnNames = {"parent_id", "name"})
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 255)
    private String imagePublicId;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}
