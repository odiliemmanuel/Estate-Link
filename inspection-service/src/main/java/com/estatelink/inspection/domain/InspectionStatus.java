package com.estatelink.inspection.domain;

public enum InspectionStatus {
    PENDING,    // requested by applicant, awaiting agent decision
    ACCEPTED,   // agent confirmed the inspection
    DECLINED,   // agent rejected the request
    CANCELLED,  // applicant withdrew or slot was cancelled
    COMPLETED
}
