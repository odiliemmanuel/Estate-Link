package com.estatelink.inspection.service;

import com.estatelink.inspection.domain.InspectionSlot;
import com.estatelink.inspection.domain.SlotStatus;
import com.estatelink.inspection.dto.requests.CreateSlotRequest;
import com.estatelink.inspection.exception.SlotConflictException;
import com.estatelink.inspection.exception.SlotNotFoundException;
import com.estatelink.inspection.exception.UnauthorizedActionException;
import com.estatelink.inspection.repository.InspectionSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InspectionSlotService {

    private final InspectionSlotRepository slotRepository;

    public InspectionSlotService(InspectionSlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Transactional
    public InspectionSlot createSlot(UUID agentId, CreateSlotRequest request) {
        assertNoOverlap(request.getListingId(), request.getSlotStart(), request.getSlotEnd());

        InspectionSlot slot = InspectionSlot.builder()
                .listingId(request.getListingId())
                .agentId(agentId)
                .slotStart(request.getSlotStart())
                .slotEnd(request.getSlotEnd())
                .status(SlotStatus.OPEN)
                .build();

        return slotRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public InspectionSlot getSlot(UUID slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Inspection slot not found: " + slotId));
    }

    @Transactional(readOnly = true)
    public List<InspectionSlot> getSlotsForListing(UUID listingId) {
        return slotRepository.findByListingId(listingId);
    }

    @Transactional(readOnly = true)
    public List<InspectionSlot> getSlotsForListingOpen(UUID listingId) {
        return slotRepository.findByListingIdAndStatus(listingId, SlotStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<InspectionSlot> getMySlots(UUID agentId) {
        return slotRepository.findByAgentId(agentId);
    }

    /**
     * Cancels an agent's slot. If the slot was booked, the associated request
     * is released back to CANCELLED so the time window is free again.
     */
    @Transactional
    public void cancelSlot(UUID agentId, UUID slotId) {
        InspectionSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Inspection slot not found: " + slotId));

        if (!slot.getAgentId().equals(agentId)) {
            throw new UnauthorizedActionException("Only the slot's agent can cancel it");
        }
        if (slot.getStatus() == SlotStatus.CANCELLED) {
            throw new SlotConflictException("Slot is already cancelled");
        }

        slot.setStatus(SlotStatus.CANCELLED);
        slotRepository.save(slot);
    }

    private void assertNoOverlap(UUID listingId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        List<InspectionSlot> overlapping = slotRepository.findOverlapping(listingId, start, end);
        if (!overlapping.isEmpty()) {
            InspectionSlot conflict = overlapping.get(0);
            throw new SlotConflictException(
                    "Slot conflicts with existing slot " + conflict.getId()
                            + " (" + conflict.getSlotStart() + " - " + conflict.getSlotEnd() + ")");
        }
    }
}
