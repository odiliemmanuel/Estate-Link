package com.estatelink.property.controller;

import com.estatelink.property.dto.requests.CreateListingRequest;
import com.estatelink.property.dto.requests.UpdateListingRequest;
import com.estatelink.property.dto.responses.ListingResponse;
import com.estatelink.property.service.ListingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody CreateListingRequest request, HttpServletRequest httpRequest) {

        UUID requesterId = extractUserId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingService.createListing(request, requesterId));
    }

    @GetMapping
    public ResponseEntity<List<ListingResponse>> getActiveListings() {
        return ResponseEntity.ok(listingService.getActiveListings());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListingResponse>> getPendingListings() {
        return ResponseEntity.ok(listingService.getPendingListings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateListingRequest request) {
        return ResponseEntity.ok(listingService.updateListing(id, request));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ListingResponse>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(listingService.getListingsByProperty(propertyId));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingResponse> approve(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID adminId = extractUserId(httpRequest);
        return ResponseEntity.ok(listingService.approveListing(id, adminId));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListingResponse> reject(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID adminId = extractUserId(httpRequest);
        return ResponseEntity.ok(listingService.rejectListing(id, adminId));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ListingResponse> suspend(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID requesterId = extractUserId(httpRequest);
        return ResponseEntity.ok(listingService.suspendListing(id, requesterId));
    }

    private UUID extractUserId(HttpServletRequest request) {
        return UUID.fromString((String) request.getAttribute("userId"));
    }
}
