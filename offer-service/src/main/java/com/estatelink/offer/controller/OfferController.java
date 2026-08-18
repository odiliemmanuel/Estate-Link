package com.estatelink.offer.controller;

import com.estatelink.offer.dto.requests.CreateOfferRequest;
import com.estatelink.offer.dto.responses.OfferResponse;
import com.estatelink.offer.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<OfferResponse> createOffer(Authentication auth,
                                                     @Valid @RequestBody CreateOfferRequest request) {
        UUID agentId = (UUID) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offerService.createOffer(agentId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OfferResponse>> getMyOffers(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(offerService.getMyOffers(userId));
    }

    @GetMapping("/listing/{listingId}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<OfferResponse>> getOffersForListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(offerService.getOffersForListing(listingId));
    }

    @PatchMapping("/{offerId}/accept")
    @PreAuthorize("hasAnyRole('AGENT', 'APPLICANT')")
    public ResponseEntity<OfferResponse> acceptOffer(Authentication auth, @PathVariable UUID offerId) {
        UUID requesterId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(offerService.acceptOffer(requesterId, offerId, auth));
    }

    @PatchMapping("/{offerId}/reject")
    @PreAuthorize("hasAnyRole('AGENT', 'APPLICANT')")
    public ResponseEntity<OfferResponse> rejectOffer(Authentication auth, @PathVariable UUID offerId) {
        UUID requesterId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(offerService.rejectOffer(requesterId, offerId, auth));
    }

    @PatchMapping("/{offerId}/withdraw")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<OfferResponse> withdrawOffer(Authentication auth, @PathVariable UUID offerId) {
        UUID agentId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(offerService.withdrawOffer(agentId, offerId));
    }
}
