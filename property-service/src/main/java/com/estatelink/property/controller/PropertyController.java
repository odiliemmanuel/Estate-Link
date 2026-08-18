package com.estatelink.property.controller;

import com.estatelink.property.domain.AvailabilityStatus;
import com.estatelink.property.dto.requests.AssignAgentRequest;
import com.estatelink.property.dto.requests.CreatePropertyRequest;
import com.estatelink.property.dto.responses.PropertyResponse;
import com.estatelink.property.service.PropertyService;
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
@RequestMapping("api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;


    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody CreatePropertyRequest createPropertyRequest, HttpServletRequest httpServletRequest){
        UUID ownerId = extractUserId(httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(createPropertyRequest, ownerId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable UUID id){
        return ResponseEntity.ok(propertyService.getProperty(id));
    }


    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<List<PropertyResponse>> getMyProperties(HttpServletRequest httpRequest) {
        UUID userId = extractUserId(httpRequest);
        return ResponseEntity.ok(propertyService.getMyProperties(userId));
    }

    @PatchMapping("/{id}/agent")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> assignAgent(
            @PathVariable UUID id,
            @Valid @RequestBody AssignAgentRequest request,
            HttpServletRequest httpRequest) {
        UUID ownerId = extractUserId(httpRequest);
        return ResponseEntity.ok(
                propertyService.assignAgent(id, request.getAgentId(), ownerId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PropertyResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam AvailabilityStatus availabilityStatus,
            HttpServletRequest httpRequest) {
        UUID requesterId = extractUserId(httpRequest);
        return ResponseEntity.ok(propertyService.updateStatus(id, availabilityStatus, requesterId));
    }

    @PatchMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    public ResponseEntity<PropertyResponse> updateImages(
            @PathVariable UUID id,
            @RequestBody List<String> imageUrls,
            HttpServletRequest httpRequest) {
        UUID requesterId = extractUserId(httpRequest);
        String requesterRole = (String) httpRequest.getAttribute("role");
        return ResponseEntity.ok(propertyService.updateImages(id, imageUrls, requesterId, requesterRole));
    }

    private UUID extractUserId(HttpServletRequest request) {
        return UUID.fromString((String) request.getAttribute("userId"));
    }
}