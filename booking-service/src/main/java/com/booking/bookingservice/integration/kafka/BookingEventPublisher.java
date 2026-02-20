package com.booking.bookingservice.integration.kafka;

public interface BookingEventPublisher {
  void publish(BookingEventMessage event);
}
