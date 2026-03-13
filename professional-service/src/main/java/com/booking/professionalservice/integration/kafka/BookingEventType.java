package com.booking.professionalservice.integration.kafka;

/**
 * Event types published by booking-service.
 */
public enum BookingEventType {
  BOOKING_CREATED,
  BOOKING_RESCHEDULED,
  BOOKING_CANCELLED
}
