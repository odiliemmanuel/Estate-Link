package com.estatelink.inspection.exception;

import com.estatelink.inspection.domain.InspectionStatus;
import com.estatelink.inspection.domain.SlotStatus;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(SlotStatus current, SlotStatus target) {
        super("Cannot move inspection slot from " + current + " to " + target);
    }

    public InvalidStateTransitionException(InspectionStatus current, InspectionStatus target) {
        super("Cannot move inspection request from " + current + " to " + target);
    }
}
