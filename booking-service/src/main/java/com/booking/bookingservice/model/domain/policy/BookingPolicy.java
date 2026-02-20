package com.booking.bookingservice.model.domain.policy;

import java.time.LocalDateTime;

public interface BookingPolicy {
  void validate(LocalDateTime startAt, LocalDateTime endAt, int durationHours);
}
