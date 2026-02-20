package com.booking.bookingservice.model.domain.schedule;

import com.booking.bookingservice.model.entity.BookingEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public final class ScheduleRules {
  private ScheduleRules() {}

  public static final Duration BREAK_BETWEEN_APPOINTMENTS = Duration.ofMinutes(30);

  public static boolean isAvailable(LocalDateTime newStart, LocalDateTime newEnd, List<BookingEntity> existing) {
    for (var b : existing) {
      var s = b.getStartAt();
      var e = b.getEndAt();

      boolean overlaps = newStart.isBefore(e) && newEnd.isAfter(s);
      if (overlaps) return false;

      if (!newStart.isBefore(e)) { // newStart >= e
        if (Duration.between(e, newStart).compareTo(BREAK_BETWEEN_APPOINTMENTS) < 0) return false;
      }

      if (!newEnd.isAfter(s)) { // newEnd <= s
        if (Duration.between(newEnd, s).compareTo(BREAK_BETWEEN_APPOINTMENTS) < 0) return false;
      }
    }
    return true;
  }
}
