package com.booking.bookingservice.integration.kafka;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A simple event payload published to Kafka as JSON (String).
 */
public record BookingEventMessage(
    BookingEventType type,
    String bookingId,
    String vehicleId,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    int durationHours,
    List<String> cleanerIds,
    OffsetDateTime occurredAt
) {}
