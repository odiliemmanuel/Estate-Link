package com.estatelink.inspection.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateInspectionRequest {

    @NotNull(message = "slotId is required")
    private UUID slotId;

    @Size(max = 1000, message = "message must be at most 1000 characters")
    private String message;
}
