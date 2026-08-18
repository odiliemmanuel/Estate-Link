package com.estatelink.property.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID propertyId;      // references Property in same service — real FK is fine here

    @Column(nullable = false)
    private UUID ownerId;

    private UUID agentId;

    private UUID approvedBy;      // admin userId who approved

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;      

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    private boolean approved = false;

    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}