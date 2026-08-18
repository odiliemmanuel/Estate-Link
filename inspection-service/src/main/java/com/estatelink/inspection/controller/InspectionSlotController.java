package com.estatelink.inspection.controller;

import com.estatelink.inspection.domain.InspectionSlot;
import com.estatelink.inspection.dto.requests.CreateSlotRequest;
import com.estatelink.inspection.dto.responses.InspectionSlotResponse;
import com.estatelink.inspection.service.InspectionSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspection-slots")
public class InspectionSlotController {

    private final InspectionSlotService slotService;

    public InspectionSlotController(InspectionSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<InspectionSlotResponse> createSlot(Authentication auth,
                                                             @Valid @RequestBody CreateSlotRequest request) {
        UUID agentId = (UUID) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InspectionSlotResponse.from(slotService.createSlot(agentId, request)));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<InspectionSlotResponse> getSlot(@PathVariable UUID slotId) {
        return ResponseEntity.ok(InspectionSlotResponse.from(slotService.getSlot(slotId)));
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<InspectionSlotResponse>> getSlotsForListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(slotService.getSlotsForListing(listingId).stream()
                .map(InspectionSlotResponse::from)
                .toList());
    }

    @GetMapping("/listing/{listingId}/open")
    public ResponseEntity<List<InspectionSlotResponse>> getOpenSlotsForListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(slotService.getSlotsForListingOpen(listingId).stream()
                .map(InspectionSlotResponse::from)
                .toList());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<InspectionSlotResponse>> getMySlots(Authentication auth) {
        UUID agentId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(slotService.getMySlots(agentId).stream()
                .map(InspectionSlotResponse::from)
                .toList());
    }

    @PatchMapping("/{slotId}/cancel")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<InspectionSlotResponse> cancelSlot(Authentication auth, @PathVariable UUID slotId) {
        UUID agentId = (UUID) auth.getPrincipal();
        slotService.cancelSlot(agentId, slotId);
        return ResponseEntity.ok(InspectionSlotResponse.from(slotService.getSlot(slotId)));
    }
}
