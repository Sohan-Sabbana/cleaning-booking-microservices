package com.booking.bookingservice.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateBookingRequest(
    @NotNull LocalDateTime newStartAt,
    @Min(2) @Max(4) int newDurationHours
) {}
