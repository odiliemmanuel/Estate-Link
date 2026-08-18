package com.estatelink.inspection.dto.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSlotRequest {

    @NotNull(message = "listingId is required")
    private UUID listingId;

    @NotNull(message = "slotStart is required")
    private LocalDateTime slotStart;

    @NotNull(message = "slotEnd is required")
    private LocalDateTime slotEnd;

    @AssertTrue(message = "slotEnd must be after slotStart")
    public boolean isEndAfterStart() {
        return slotEnd == null || slotStart == null || slotEnd.isAfter(slotStart);
    }

    @AssertTrue(message = "slotStart must be in the future")
    public boolean isInFuture() {
        return slotStart == null || slotStart.isAfter(LocalDateTime.now());
    }
}
