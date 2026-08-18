package com.estatelink.inspection.dto.responses;

import com.estatelink.inspection.domain.InspectionSlot;
import com.estatelink.inspection.domain.SlotStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InspectionSlotResponse {

    private UUID id;
    private UUID listingId;
    private UUID agentId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private SlotStatus status;
    private LocalDateTime createdAt;

    public static InspectionSlotResponse from(InspectionSlot slot) {
        return InspectionSlotResponse.builder()
                .id(slot.getId())
                .listingId(slot.getListingId())
                .agentId(slot.getAgentId())
                .slotStart(slot.getSlotStart())
                .slotEnd(slot.getSlotEnd())
                .status(slot.getStatus())
                .createdAt(slot.getCreatedAt())
                .build();
    }
}
