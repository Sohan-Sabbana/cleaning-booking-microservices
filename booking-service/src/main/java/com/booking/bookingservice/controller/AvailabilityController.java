package com.booking.bookingservice.controller;

import com.booking.bookingservice.model.dto.response.AvailabilityByDateResponse;
import com.booking.bookingservice.model.dto.response.AvailabilityForSlotResponse;
import com.booking.bookingservice.service.AvailabilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Tag(name = "Availability")
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

  private final AvailabilityService availabilityService;

  public AvailabilityController(AvailabilityService availabilityService) {
    this.availabilityService = availabilityService;
  }

  @GetMapping
  public AvailabilityByDateResponse availabilityByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    var availability = availabilityService.availabilityByDate(date);
    var vehicles = availability.entrySet().stream()
        .map(vEntry -> new AvailabilityByDateResponse.VehicleAvailability(
            vEntry.getKey(),
            vEntry.getValue().entrySet().stream()
                .map(cEntry -> new AvailabilityByDateResponse.CleanerAvailability(
                    cEntry.getKey(),
                    cEntry.getValue().get(2),
                    cEntry.getValue().get(4)
                ))
                .toList()
        ))
        .toList();

    return new AvailabilityByDateResponse(date, vehicles);
  }

  @GetMapping("/slot")
  public AvailabilityForSlotResponse availabilityForSlot(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
      @RequestParam @Min(2) @Max(4) int durationHours,
      @RequestParam(defaultValue = "1") @Min(1) @Max(3) int professionalCount
  ) {
    var vehicles = availabilityService.vehicles().stream()
        .map(v -> new AvailabilityForSlotResponse.VehicleCandidate(
            v.id(),
            availabilityService.availableCleanersFor(v.id(), startAt, durationHours)
        ))
        .filter(v -> v.availableCleanerIds().size() >= professionalCount)
        .toList();

    return new AvailabilityForSlotResponse(startAt, durationHours, professionalCount, vehicles);
  }
}
