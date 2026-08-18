package com.estatelink.property.repository;

import com.estatelink.property.domain.Listing;
import com.estatelink.property.domain.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findByPropertyId(UUID propertyId);
    List<Listing> findByOwnerId(UUID ownerId);
    List<Listing> findByAgentId(UUID agentId);
    
}
