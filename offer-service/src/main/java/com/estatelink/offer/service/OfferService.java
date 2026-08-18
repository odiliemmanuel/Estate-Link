package com.estatelink.offer.service;

import com.estatelink.common.event.OfferAcceptedEvent;
import com.estatelink.common.event.OfferRejectedEvent;
import com.estatelink.common.event.OfferSentEvent;
import com.estatelink.offer.domain.Offer;
import com.estatelink.offer.domain.OfferStatus;
import com.estatelink.offer.dto.requests.CreateOfferRequest;
import com.estatelink.offer.dto.responses.OfferResponse;
import com.estatelink.offer.eventProducer.OfferEventProducer;
import com.estatelink.offer.exception.InvalidStateTransitionException;
import com.estatelink.offer.exception.OfferNotFoundException;
import com.estatelink.offer.exception.UnauthorizedActionException;
import com.estatelink.offer.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferEventProducer eventProducer;

    @Transactional
    public OfferResponse createOffer(UUID agentId, CreateOfferRequest request) {
        Offer offer = Offer.builder()
                .listingId(request.getListingId())
                .applicantId(request.getApplicantId())
                .agentId(agentId)
                .amount(request.getAmount())
                .type(request.getType())
                .note(request.getNote())
                .status(OfferStatus.SENT)
                .build();

        Offer saved = offerRepository.save(offer);

        eventProducer.publishOfferSent(OfferSentEvent.builder()
                .offerId(saved.getId())
                .listingId(saved.getListingId())
                .agentId(saved.getAgentId())
                .applicantId(saved.getApplicantId())
                .amount(saved.getAmount())
                .type(saved.getType().name())
                .note(saved.getNote())
                .sentAt(LocalDateTime.now())
                .build());

        return OfferResponse.from(saved);
    }

    public List<OfferResponse> getMyOffers(UUID userId) {
        List<Offer> offers = new ArrayList<>();
        offers.addAll(offerRepository.findByApplicantId(userId));
        offers.addAll(offerRepository.findByAgentId(userId));
        return offers.stream().map(OfferResponse::from).toList();
    }

    public List<OfferResponse> getOffersForListing(UUID listingId) {
        return offerRepository.findByListingId(listingId).stream()
                .map(OfferResponse::from).toList();
    }

    @Transactional
    public OfferResponse acceptOffer(UUID requesterId, UUID offerId, Authentication authentication) {
        Offer offer = findById(offerId);
        assertCanDecide(offer, requesterId, authentication);
        assertState(offer, OfferStatus.SENT, "Only a SENT offer can be accepted");

        offer.setStatus(OfferStatus.ACCEPTED);
        Offer saved = offerRepository.save(offer);

        eventProducer.publishOfferAccepted(OfferAcceptedEvent.builder()
                .offerId(saved.getId())
                .listingId(saved.getListingId())
                .agentId(saved.getAgentId())
                .applicantId(saved.getApplicantId())
                .amount(saved.getAmount())
                .type(saved.getType().name())
                .acceptedAt(LocalDateTime.now())
                .build());

        return OfferResponse.from(saved);
    }

    @Transactional
    public OfferResponse rejectOffer(UUID requesterId, UUID offerId, Authentication authentication) {
        Offer offer = findById(offerId);
        assertCanDecide(offer, requesterId, authentication);
        assertState(offer, OfferStatus.SENT, "Only a SENT offer can be rejected");

        offer.setStatus(OfferStatus.REJECTED);
        Offer saved = offerRepository.save(offer);

        eventProducer.publishOfferRejected(OfferRejectedEvent.builder()
                .offerId(saved.getId())
                .listingId(saved.getListingId())
                .agentId(saved.getAgentId())
                .applicantId(saved.getApplicantId())
                .amount(saved.getAmount())
                .rejectedAt(LocalDateTime.now())
                .build());

        return OfferResponse.from(saved);
    }

    @Transactional
    public OfferResponse withdrawOffer(UUID agentId, UUID offerId) {
        Offer offer = findById(offerId);

        if (!offer.getAgentId().equals(agentId)) {
            throw new UnauthorizedActionException("Only the agent who sent the offer can withdraw it");
        }
        assertState(offer, OfferStatus.SENT, "Only a SENT offer can be withdrawn");

        offer.setStatus(OfferStatus.WITHDRAWN);
        return OfferResponse.from(offerRepository.save(offer));
    }

    private void assertCanDecide(Offer offer, UUID requesterId, Authentication authentication) {
        boolean isAgent = hasRole(authentication, "ROLE_AGENT");
        boolean isApplicant = hasRole(authentication, "ROLE_APPLICANT");

        if (isAgent && !offer.getAgentId().equals(requesterId)) {
            throw new UnauthorizedActionException("Only the agent who sent the offer can action it");
        }
        if (isApplicant && !offer.getApplicantId().equals(requesterId)) {
            throw new UnauthorizedActionException("Only the applicant on the offer can action it");
        }
        if (!isAgent && !isApplicant) {
            throw new UnauthorizedActionException("Only the agent or the applicant can action this offer");
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private void assertState(Offer offer, OfferStatus expected, String message) {
        if (offer.getStatus() != expected) {
            throw new InvalidStateTransitionException(message);
        }
    }

    private Offer findById(UUID id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found: " + id));
    }
}
