package com.booking.bookingservice.model.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateBookingRequest(
    @NotNull LocalDateTime startAt,
    @Min(2) @Max(4) int durationHours,
    @Min(1) @Max(3) int professionalCount
) {}
