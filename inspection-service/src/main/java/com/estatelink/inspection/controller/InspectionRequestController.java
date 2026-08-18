package com.estatelink.inspection.controller;

import com.estatelink.inspection.domain.InspectionRequest;
import com.estatelink.inspection.dto.requests.CreateInspectionRequest;
import com.estatelink.inspection.dto.responses.InspectionRequestResponse;
import com.estatelink.inspection.service.InspectionRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspection-requests")
public class InspectionRequestController {

    private final InspectionRequestService requestService;

    public InspectionRequestController(InspectionRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<InspectionRequestResponse> createRequest(Authentication auth,
                                                                   @Valid @RequestBody CreateInspectionRequest request) {
        UUID applicantId = (UUID) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InspectionRequestResponse.from(requestService.createRequest(applicantId, request)));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<InspectionRequestResponse> getRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(InspectionRequestResponse.from(requestService.getRequest(requestId)));
    }

    @GetMapping("/my")
    public ResponseEntity<List<InspectionRequestResponse>> getMyRequests(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(requestService.getMyRequests(userId).stream()
                .map(InspectionRequestResponse::from)
                .toList());
    }

    @PatchMapping("/{requestId}/accept")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<InspectionRequestResponse> acceptRequest(Authentication auth, @PathVariable UUID requestId) {
        UUID agentId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(InspectionRequestResponse.from(requestService.acceptRequest(agentId, requestId)));
    }

    @PatchMapping("/{requestId}/decline")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<InspectionRequestResponse> declineRequest(Authentication auth, @PathVariable UUID requestId) {
        UUID agentId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(InspectionRequestResponse.from(requestService.declineRequest(agentId, requestId)));
    }

    @PatchMapping("/{requestId}/cancel")
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<InspectionRequestResponse> cancelRequest(Authentication auth, @PathVariable UUID requestId) {
        UUID applicantId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(InspectionRequestResponse.from(requestService.cancelRequest(applicantId, requestId)));
    }
}
