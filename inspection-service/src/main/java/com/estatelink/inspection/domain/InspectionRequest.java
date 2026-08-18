package com.estatelink.inspection.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An applicant's request to inspect a listing at a specific slot.
 * Exactly one active request may exist per slot (the slot flips to BOOKED).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "inspection_requests")
public class InspectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID slotId;

    @Column(nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private UUID applicantId;

    private UUID agentId;       // denormalised from the slot for easy filtering

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionStatus status = InspectionStatus.PENDING;

    @Column(length = 1000)
    private String message;     // optional note from the applicant

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
