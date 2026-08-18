package com.estatelink.analytics.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks the latest observed status of each listing, derived from domain
 * events. Gives the admin dashboard a "listings by status" breakdown.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "listing_analytics")
public class ListingAnalytic {

    @Id
    private UUID listingId;

    @Column(nullable = false)
    private String status;

    private LocalDateTime approvedAt;

    private LocalDateTime lastEventAt;
}
