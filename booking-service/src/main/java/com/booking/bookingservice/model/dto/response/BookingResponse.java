package com.booking.bookingservice.model.dto.response;

import java.time.LocalDateTime;

public record BookingResponse(String id, LocalDateTime startAt, LocalDateTime endAt, int durationHours, String vehicleId) {}
