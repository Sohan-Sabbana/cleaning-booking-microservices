package com.booking.bookingservice.model.domain.policy;

import com.booking.bookingservice.exception.DomainException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class NotFridayPolicy implements BookingPolicy {
  @Override
  public void validate(LocalDateTime startAt, LocalDateTime endAt, int durationHours) {
    if (startAt.toLocalDate().getDayOfWeek() == DayOfWeek.FRIDAY) {
      throw new DomainException("Cleaners are not working on Fridays.");
    }
  }
}
