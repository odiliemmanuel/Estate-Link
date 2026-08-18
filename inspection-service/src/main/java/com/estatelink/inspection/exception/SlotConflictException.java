package com.estatelink.inspection.exception;

/**
 * Thrown when an inspection slot conflicts with an existing slot for the
 * same listing, or when a slot is no longer bookable.
 */
public class SlotConflictException extends RuntimeException {
    public SlotConflictException(String message) {
        super(message);
    }
}
