package com.estatelink.property.service;


import com.estatelink.property.domain.Listing;
import com.estatelink.property.domain.ListingStatus;
import com.estatelink.property.domain.Property;
import com.estatelink.property.dto.requests.CreateListingRequest;
import com.estatelink.property.dto.requests.UpdateListingRequest;
import com.estatelink.property.dto.responses.ListingResponse;
import com.estatelink.property.event.ListingApprovedEvent;
import com.estatelink.property.eventProducer.PropertyEventProducer;
import com.estatelink.property.exception.ListingNotFoundException;
import com.estatelink.property.exception.PropertyNotFoundException;
import com.estatelink.property.exception.UnauthorizedException;
import com.estatelink.property.repository.ListingRepository;
import com.estatelink.property.repository.PropertyRepository;
import com.estatelink.property.utils.ListingMapper;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyEventProducer eventProducer;
    private final ListingMapper listingMapper;

    @Transactional
    public ListingResponse createListing(CreateListingRequest request, UUID requesterId) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(
                        "Property not found: " + request.getPropertyId()));

        // Only the owner or their assigned agent can create a listing
        boolean isOwner = property.getOwnerId().equals(requesterId);
        boolean isAgent = property.getAgentId() != null
                && property.getAgentId().equals(requesterId);

        if (!isOwner && !isAgent) {
            throw new UnauthorizedException("Only the property owner or assigned agent can create a listing");
        }

        Listing listing = Listing.builder()
                .propertyId(property.getId())
                .ownerId(property.getOwnerId())
                .agentId(property.getAgentId())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .purpose(request.getPurpose())
                .status(ListingStatus.PENDING_APPROVAL)  // always needs admin approval
                .approved(false)
                .build();

        return listingMapper.toResponse(listingRepository.save(listing));
    }

    public List<ListingResponse> getActiveListings() {
        return listingRepository.findByStatus(ListingStatus.ACTIVE)
                .stream().map(listingMapper::toResponse).toList();
    }

    public List<ListingResponse> getListingsByProperty(UUID propertyId) {
        return listingRepository.findByPropertyId(propertyId)
                .stream().map(listingMapper::toResponse).toList();
    }

    public List<ListingResponse> getPendingListings() {
        return listingRepository.findByStatus(ListingStatus.PENDING_APPROVAL)
                .stream().map(listingMapper::toResponse).toList();
    }

    public ListingResponse getListing(UUID listingId) {
        return listingMapper.toResponse(findById(listingId));
    }

    // ── Admin actions ────────────────────────────────────────────────────

    @Transactional
    public ListingResponse approveListing(UUID listingId, UUID adminId) {
        Listing listing = findById(listingId);

        listing.setStatus(ListingStatus.ACTIVE);
        listing.setApproved(true);
        listing.setApprovedBy(adminId);
        listing.setApprovedAt(LocalDateTime.now());

        Listing savedListing = listingRepository.save(listing);

        // Publish Kafka event — notification svc will email the owner
        eventProducer.sendListingApprovedEvent(new ListingApprovedEvent(
                savedListing.getId(),
                savedListing.getPropertyId(),
                savedListing.getOwnerId(),
                savedListing.getAgentId(),
                savedListing.getTitle(),
                null,            // ownerEmail: fetch from user-service or pass via JWT — see note below
                savedListing.getApprovedAt()
        ));

        return listingMapper.toResponse(savedListing);
    }

    @Transactional
    public ListingResponse rejectListing(UUID listingId, UUID adminId) {
        Listing listing = findById(listingId);
        listing.setStatus(ListingStatus.REJECTED);
        return listingMapper.toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse suspendListing(UUID listingId, UUID requesterId) {
        Listing listing = findById(listingId);
        listing.setStatus(ListingStatus.SUSPENDED);
        return listingMapper.toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse updateListing(UUID listingId, UpdateListingRequest request) {
        Listing listing = findById(listingId);
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setPrice(request.getPrice());
        return listingMapper.toResponse(listingRepository.save(listing));
    }

    // ── Offer-driven status transitions ──────────────────────────────────

    @Transactional
    public void markUnderOffer(UUID listingId) {
        listingRepository.findById(listingId)
                .filter(listing -> listing.getStatus() == ListingStatus.ACTIVE)
                .ifPresent(listing -> {
                    listing.setStatus(ListingStatus.UNDER_OFFER);
                    listingRepository.save(listing);
                });
    }

    @Transactional
    public void closeListing(UUID listingId) {
        listingRepository.findById(listingId)
                .ifPresent(listing -> {
                    listing.setStatus(ListingStatus.CLOSED);
                    listingRepository.save(listing);
                });
    }

    private Listing findById(UUID id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found: " + id));
    }
}
