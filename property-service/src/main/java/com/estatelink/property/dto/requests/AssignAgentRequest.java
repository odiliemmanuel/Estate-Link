package com.estatelink.property.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AssignAgentRequest {

    @NotNull
    private UUID agentId;
}
