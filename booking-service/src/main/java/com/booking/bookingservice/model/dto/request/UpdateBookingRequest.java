package com.booking.bookingservice.model.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record UpdateBookingRequest(
    @NotNull LocalDateTime newStartAt,
    @Min(2) @Max(4) int newDurationHours
) {}
