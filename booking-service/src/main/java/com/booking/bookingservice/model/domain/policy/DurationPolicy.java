package com.booking.bookingservice.model.domain.policy;

import com.booking.bookingservice.exception.DomainException;

import java.time.LocalDateTime;

public class DurationPolicy implements BookingPolicy {
  @Override
  public void validate(LocalDateTime startAt, LocalDateTime endAt, int durationHours) {
    if (durationHours != 2 && durationHours != 4) {
      throw new DomainException("Duration must be 2 or 4 hours.");
    }
    if (!endAt.equals(startAt.plusHours(durationHours))) {
      throw new DomainException("End time must equal start time + duration.");
    }
  }
}
