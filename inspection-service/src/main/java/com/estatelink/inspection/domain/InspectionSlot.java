package com.estatelink.inspection.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A block of time an agent offers for viewing a listing.
 * Slots for the same listing must not overlap — enforced in the service
 * layer (InspectionSlotService) with a DB unique constraint as a backstop.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "inspection_slots",
       uniqueConstraints = @UniqueConstraint(name = "uk_slot_listing_start", columnNames = {"listingId", "slotStart"}))
public class InspectionSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID listingId;     // references Listing in property-service

    @Column(nullable = false)
    private UUID agentId;       // user-service agent who owns this slot

    @Column(nullable = false)
    private LocalDateTime slotStart;

    @Column(nullable = false)
    private LocalDateTime slotEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.OPEN;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
