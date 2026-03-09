package com.booking.bookingservice.service;

import com.booking.bookingservice.integration.professionals.ProfessionalsClient;
import com.booking.bookingservice.integration.professionals.dto.VehicleDto;
import com.booking.bookingservice.model.domain.policy.DurationPolicy;
import com.booking.bookingservice.model.domain.policy.NotFridayPolicy;
import com.booking.bookingservice.model.domain.policy.PolicyChain;
import com.booking.bookingservice.model.domain.policy.WorkingHoursPolicy;
import com.booking.bookingservice.model.domain.schedule.ScheduleRules;
import com.booking.bookingservice.model.entity.BookingEntity;
import com.booking.bookingservice.model.enums.BookingStatus;
import com.booking.bookingservice.repository.BookingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class AvailabilityService {

  private static final LocalTime START = LocalTime.of(8, 0);
  private static final LocalTime END = LocalTime.of(22, 0);
  private static final int STEP_MINUTES = 30;

  private final ProfessionalsGateway professionalsGateway;
  private final BookingRepository bookingRepository;
  private final PolicyChain policyChain;

  public AvailabilityService(ProfessionalsGateway professionalsGateway, BookingRepository bookingRepository) {
    this.professionalsGateway = professionalsGateway;
    this.bookingRepository = bookingRepository;
    this.policyChain = new PolicyChain(List.of(new NotFridayPolicy(), new WorkingHoursPolicy(), new DurationPolicy()));
  }

  public List<VehicleDto> vehicles() {
    return professionalsGateway.listVehicles();
  }

  @Cacheable(cacheNames = "booking-availability-by-date", key = "#date.toString()")
  public Map<String, Map<String, Map<Integer, List<LocalTime>>>> availabilityByDate(LocalDate date) {
    var result = new LinkedHashMap<String, Map<String, Map<Integer, List<LocalTime>>>>();

    for (var v : professionalsGateway.listVehicles()) {
      var byCleaner = new LinkedHashMap<String, Map<Integer, List<LocalTime>>>();
      for (var c : v.cleaners()) {
        var dayFrom = date.atStartOfDay();
        var dayTo = date.plusDays(1).atStartOfDay();
        var existing = bookingRepository.findActiveBookingsForCleanerWithin(c.id(), BookingStatus.ACTIVE, dayFrom, dayTo);

        byCleaner.put(c.id(), Map.of(
            2, computeStartTimes(date, 2, existing),
            4, computeStartTimes(date, 4, existing)
        ));
      }
      result.put(v.id(), byCleaner);
    }

    return result;
  }

  @Cacheable(cacheNames = "booking-availability-slot", key = "#vehicleId + '|' + #startAt.toString() + '|' + #durationHours")
  public List<String> availableCleanersFor(String vehicleId, LocalDateTime startAt, int durationHours) {
    var endAt = startAt.plusHours(durationHours);
    policyChain.validate(startAt, endAt, durationHours);

    var vehicle = professionalsGateway.listVehicles().stream()
        .filter(v -> v.id().equals(vehicleId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown vehicleId: " + vehicleId));

    var from = startAt.toLocalDate().atStartOfDay();
    var to = startAt.toLocalDate().plusDays(1).atStartOfDay();

    List<String> available = new ArrayList<>();
    for (var c : vehicle.cleaners()) {
      var existing = bookingRepository.findActiveBookingsForCleanerWithin(c.id(), BookingStatus.ACTIVE, from, to);
      if (ScheduleRules.isAvailable(startAt, endAt, existing)) {
        available.add(c.id());
      }
    }
    return available;
  }

  public Optional<VehicleCandidate> findVehicleWithCapacity(LocalDateTime startAt, int durationHours, int professionalCount) {
    var endAt = startAt.plusHours(durationHours);
    policyChain.validate(startAt, endAt, durationHours);

    for (var v : professionalsGateway.listVehicles()) {
      var avail = availableCleanersFor(v.id(), startAt, durationHours);
      if (avail.size() >= professionalCount) {
        return Optional.of(new VehicleCandidate(v.id(), avail));
      }
    }
    return Optional.empty();
  }

  private List<LocalTime> computeStartTimes(LocalDate date, int durationHours, List<BookingEntity> existing) {
    var startAt = date.atTime(START);
    var lastStart = date.atTime(END).minusHours(durationHours);

    List<LocalTime> times = new ArrayList<>();
    for (LocalDateTime t = startAt; !t.isAfter(lastStart); t = t.plusMinutes(STEP_MINUTES)) {
      var end = t.plusHours(durationHours);
      policyChain.validate(t, end, durationHours);
      if (ScheduleRules.isAvailable(t, end, existing)) {
        times.add(t.toLocalTime());
      }
    }
    return times;
  }

  public record VehicleCandidate(String vehicleId, List<String> availableCleanerIds) {}
}
