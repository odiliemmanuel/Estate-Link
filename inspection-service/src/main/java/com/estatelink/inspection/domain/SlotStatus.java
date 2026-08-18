package com.estatelink.inspection.domain;

public enum SlotStatus {
    OPEN,          // available for applicants to request
    BOOKED,        // an inspection request is pending/accepted on it
    CANCELLED
}
