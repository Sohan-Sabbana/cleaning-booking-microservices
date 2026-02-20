package com.booking.bookingservice.model.domain.policy;

import com.booking.bookingservice.exception.DomainException;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class WorkingHoursPolicy implements BookingPolicy {

  private static final LocalTime START = LocalTime.of(8, 0);
  private static final LocalTime END = LocalTime.of(22, 0);

  @Override
  public void validate(LocalDateTime startAt, LocalDateTime endAt, int durationHours) {
    if (startAt.toLocalTime().isBefore(START)) {
      throw new DomainException("First appointment cannot start before 08:00.");
    }
    if (endAt.toLocalTime().isAfter(END)) {
      throw new DomainException("Last appointment must finish by 22:00.");
    }
  }
}
