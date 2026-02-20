package com.booking.bookingservice.integration.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Test/dev fallback when Kafka is disabled.
 */
@Service
@ConditionalOnProperty(name = "justlife.kafka.enabled", havingValue = "false")
public class NoopBookingEventPublisher implements BookingEventPublisher {
  @Override
  public void publish(BookingEventMessage event) {
    // no-op
  }
}
