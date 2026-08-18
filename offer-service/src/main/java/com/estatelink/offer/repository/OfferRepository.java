package com.estatelink.offer.repository;

import com.estatelink.offer.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {

    List<Offer> findByApplicantId(UUID applicantId);
    List<Offer> findByAgentId(UUID agentId);
    List<Offer> findByListingId(UUID listingId);
}
