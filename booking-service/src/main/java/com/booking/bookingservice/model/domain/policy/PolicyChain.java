package com.booking.bookingservice.model.domain.policy;

import java.time.LocalDateTime;
import java.util.List;

public class PolicyChain {

  private final List<BookingPolicy> policies;

  public PolicyChain(List<BookingPolicy> policies) {
    this.policies = List.copyOf(policies);
  }

  public void validate(LocalDateTime startAt, LocalDateTime endAt, int durationHours) {
    for (var p : policies) {
      p.validate(startAt, endAt, durationHours);
    }
  }
}
