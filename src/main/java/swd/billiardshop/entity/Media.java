package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import swd.billiardshop.enums.FileType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mediaId;

    @Column(length = 255, nullable = false)
    private String fileName;

    @Column(length = 500, nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private FileType fileType;

    private Integer fileSize;

    @Column(length = 255)
    private String cloudinaryPublicId;

    @Column(length = 20)
    private String entityType;
    private Integer entityId;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
