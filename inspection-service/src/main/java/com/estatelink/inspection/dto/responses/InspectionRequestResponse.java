package com.estatelink.inspection.dto.responses;

import com.estatelink.inspection.domain.InspectionRequest;
import com.estatelink.inspection.domain.InspectionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InspectionRequestResponse {

    private UUID id;
    private UUID slotId;
    private UUID listingId;
    private UUID applicantId;
    private UUID agentId;
    private InspectionStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InspectionRequestResponse from(InspectionRequest request) {
        return InspectionRequestResponse.builder()
                .id(request.getId())
                .slotId(request.getSlotId())
                .listingId(request.getListingId())
                .applicantId(request.getApplicantId())
                .agentId(request.getAgentId())
                .status(request.getStatus())
                .message(request.getMessage())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
