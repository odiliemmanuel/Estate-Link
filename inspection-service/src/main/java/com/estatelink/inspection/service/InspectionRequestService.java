package com.estatelink.inspection.service;

import com.estatelink.inspection.domain.InspectionRequest;
import com.estatelink.inspection.domain.InspectionStatus;
import com.estatelink.inspection.domain.InspectionSlot;
import com.estatelink.inspection.domain.SlotStatus;
import com.estatelink.inspection.dto.requests.CreateInspectionRequest;
import com.estatelink.inspection.event.InspectionRequestedEvent;
import com.estatelink.inspection.eventProducer.InspectionEventProducer;
import com.estatelink.inspection.exception.InspectionRequestNotFoundException;
import com.estatelink.inspection.exception.InvalidStateTransitionException;
import com.estatelink.inspection.exception.SlotConflictException;
import com.estatelink.inspection.exception.SlotNotFoundException;
import com.estatelink.inspection.exception.UnauthorizedActionException;
import com.estatelink.inspection.repository.InspectionRequestRepository;
import com.estatelink.inspection.repository.InspectionSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class InspectionRequestService {

    private final InspectionSlotRepository slotRepository;
    private final InspectionRequestRepository requestRepository;
    private final InspectionEventProducer eventProducer;

    public InspectionRequestService(InspectionSlotRepository slotRepository,
                                    InspectionRequestRepository requestRepository,
                                    InspectionEventProducer eventProducer) {
        this.slotRepository = slotRepository;
        this.requestRepository = requestRepository;
        this.eventProducer = eventProducer;
    }

    /**
     * Books a slot for an applicant. The slot is locked (PESSIMISTIC_WRITE)
     * for the duration of the transaction so two applicants can never book
     * the same slot concurrently — one will fail on the status check.
     */
    @Transactional
    public InspectionRequest createRequest(UUID applicantId, CreateInspectionRequest request) {
        InspectionSlot slot = slotRepository.findByIdForUpdate(request.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Inspection slot not found: " + request.getSlotId()));

        if (slot.getStatus() != SlotStatus.OPEN) {
            throw new SlotConflictException("Inspection slot is not available for booking");
        }
        if (slot.getSlotStart().isBefore(LocalDateTime.now())) {
            throw new SlotConflictException("Inspection slot is in the past and can no longer be booked");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        InspectionRequest inspectionRequest = InspectionRequest.builder()
                .slotId(slot.getId())
                .listingId(slot.getListingId())
                .applicantId(applicantId)
                .agentId(slot.getAgentId())
                .message(request.getMessage())
                .status(InspectionStatus.PENDING)
                .build();

        InspectionRequest saved = requestRepository.save(inspectionRequest);

        eventProducer.publishInspectionRequested(InspectionRequestedEvent.builder()
                .requestId(saved.getId())
                .slotId(slot.getId())
                .listingId(slot.getListingId())
                .agentId(slot.getAgentId())
                .applicantId(applicantId)
                .slotStart(slot.getSlotStart())
                .slotEnd(slot.getSlotEnd())
                .message(saved.getMessage())
                .requestedAt(LocalDateTime.now())
                .build());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<InspectionRequest> getMyRequests(UUID userId) {
        Set<InspectionRequest> combined = new LinkedHashSet<>();
        combined.addAll(requestRepository.findByApplicantId(userId));

        List<UUID> mySlotIds = slotRepository.findByAgentId(userId).stream()
                .map(InspectionSlot::getId)
                .toList();
        if (!mySlotIds.isEmpty()) {
            combined.addAll(requestRepository.findBySlotIdIn(mySlotIds));
        }
        return new ArrayList<>(combined);
    }

    @Transactional(readOnly = true)
    public InspectionRequest getRequest(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new InspectionRequestNotFoundException(
                        "Inspection request not found: " + requestId));
    }

    /**
     * Agent accepts a pending request. The slot stays BOOKED and the request
     * moves to ACCEPTED.
     */
    @Transactional
    public InspectionRequest acceptRequest(UUID agentId, UUID requestId) {
        InspectionRequest request = getRequestOwnedByAgent(agentId, requestId);
        assertState(request, InspectionStatus.PENDING, InspectionStatus.ACCEPTED);
        request.setStatus(InspectionStatus.ACCEPTED);
        return requestRepository.save(request);
    }

    /**
     * Agent declines a pending request, freeing the slot back to OPEN so
     * other applicants can book it.
     */
    @Transactional
    public InspectionRequest declineRequest(UUID agentId, UUID requestId) {
        InspectionRequest request = getRequestOwnedByAgent(agentId, requestId);
        assertState(request, InspectionStatus.PENDING, InspectionStatus.DECLINED);
        request.setStatus(InspectionStatus.DECLINED);
        requestRepository.save(request);
        releaseSlot(request.getSlotId());
        return request;
    }

    /**
     * Applicant withdraws their own pending request, freeing the slot.
     */
    @Transactional
    public InspectionRequest cancelRequest(UUID applicantId, UUID requestId) {
        InspectionRequest request = requestRepository.findByIdAndApplicantId(requestId, applicantId)
                .orElseThrow(() -> new UnauthorizedActionException(
                        "Only the requesting applicant can cancel this request"));

        assertState(request, InspectionStatus.PENDING, InspectionStatus.CANCELLED);
        request.setStatus(InspectionStatus.CANCELLED);
        requestRepository.save(request);
        releaseSlot(request.getSlotId());
        return request;
    }

    private InspectionRequest getRequestOwnedByAgent(UUID agentId, UUID requestId) {
        return requestRepository.findByIdAndAgentId(requestId, agentId)
                .orElseThrow(() -> new UnauthorizedActionException(
                        "Only the slot's agent can action this request"));
    }

    private void assertState(InspectionRequest request, InspectionStatus expected, InspectionStatus target) {
        if (request.getStatus() != expected) {
            throw new InvalidStateTransitionException(request.getStatus(), target);
        }
    }

    private void releaseSlot(UUID slotId) {
        slotRepository.findById(slotId).ifPresent(slot -> {
            slot.setStatus(SlotStatus.OPEN);
            slotRepository.save(slot);
        });
    }
}
