package com.estatelink.offer.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateOfferRequest {

    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @NotNull(message = "Applicant ID is required")
    private UUID applicantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Offer type is required")
    private com.estatelink.offer.domain.OfferType type;

    private String note;
}
