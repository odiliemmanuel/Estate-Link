package com.estatelink.offer.dto.responses;

import com.estatelink.offer.domain.Offer;
import com.estatelink.offer.domain.OfferStatus;
import com.estatelink.offer.domain.OfferType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OfferResponse {

    private UUID id;
    private UUID listingId;
    private UUID applicantId;
    private UUID agentId;
    private BigDecimal amount;
    private OfferType type;
    private OfferStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OfferResponse from(Offer offer) {
        return OfferResponse.builder()
                .id(offer.getId())
                .listingId(offer.getListingId())
                .applicantId(offer.getApplicantId())
                .agentId(offer.getAgentId())
                .amount(offer.getAmount())
                .type(offer.getType())
                .status(offer.getStatus())
                .note(offer.getNote())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}
