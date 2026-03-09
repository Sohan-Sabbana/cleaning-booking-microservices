package com.booking.bookingservice.service;

import java.time.ZoneOffset;
import java.time.OffsetDateTime;

import com.booking.bookingservice.exception.DomainException;
import com.booking.bookingservice.integration.kafka.BookingEventMessage;
import com.booking.bookingservice.integration.kafka.BookingEventPublisher;
import com.booking.bookingservice.integration.kafka.BookingEventType;
import com.booking.bookingservice.model.entity.BookingCleanerEntity;
import com.booking.bookingservice.model.entity.BookingEntity;
import com.booking.bookingservice.model.enums.BookingStatus;
import com.booking.bookingservice.repository.BookingCleanerRepository;
import com.booking.bookingservice.repository.BookingRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingAppService {

  private final BookingRepository bookingRepository;
  private final BookingCleanerRepository bookingCleanerRepository;
  private final AvailabilityService availabilityService;
  private final BookingEventPublisher bookingEventPublisher;

  public BookingAppService(BookingRepository bookingRepository, BookingCleanerRepository bookingCleanerRepository, AvailabilityService availabilityService, BookingEventPublisher bookingEventPublisher) {
    this.bookingRepository = bookingRepository;
    this.bookingCleanerRepository = bookingCleanerRepository;
    this.availabilityService = availabilityService;
    this.bookingEventPublisher = bookingEventPublisher;
  }

  @Transactional
  @CacheEvict(cacheNames = {"booking-availability-by-date", "booking-availability-slot"}, allEntries = true)
  public BookingEntity create(LocalDateTime startAt, int durationHours, int professionalCount) {
    if (professionalCount < 1 || professionalCount > 3) {
      throw new DomainException("Cleaner professional count must be 1, 2, or 3.");
    }

    var vehicleCandidate = availabilityService.findVehicleWithCapacity(startAt, durationHours, professionalCount)
        .orElseThrow(() -> new DomainException("No available vehicle/cleaners for the requested time window."));

    var endAt = startAt.plusHours(durationHours);

    var booking = new BookingEntity(startAt, endAt, durationHours, vehicleCandidate.vehicleId(), BookingStatus.ACTIVE);
    bookingRepository.save(booking);

    String bookingId = booking.getId();

    List<String> selected = List.copyOf(vehicleCandidate.availableCleanerIds().subList(0, professionalCount));
    for (var cleanerId : selected) {
      bookingCleanerRepository.save(new BookingCleanerEntity(booking, cleanerId));
    }

    bookingEventPublisher.publish(new BookingEventMessage(
        BookingEventType.BOOKING_CREATED,
        bookingId,
        booking.getVehicleId(),
        OffsetDateTime.of(startAt, ZoneOffset.UTC),
        OffsetDateTime.of(endAt, ZoneOffset.UTC),
        durationHours,
        selected,
        OffsetDateTime.now(ZoneOffset.UTC)
    ));

    return booking;
  }

  @Transactional
  @CacheEvict(cacheNames = {"booking-availability-by-date", "booking-availability-slot"}, allEntries = true)
  public BookingEntity reschedule(String bookingId, LocalDateTime newStartAt, int newDurationHours) {
    var existing = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new DomainException("Booking not found: " + bookingId));

    var previousAssignments = bookingCleanerRepository.findByBooking_Id(bookingId);
    int professionalCount = previousAssignments.size();

    var vehicleCandidate = availabilityService.findVehicleWithCapacity(newStartAt, newDurationHours, professionalCount)
            .orElseThrow(() -> new DomainException("No availability to reschedule for the requested time window."));

    bookingCleanerRepository.deleteByBooking_Id(bookingId);

    var newEndAt = newStartAt.plusHours(newDurationHours);
    existing.reschedule(newStartAt, newEndAt, newDurationHours, vehicleCandidate.vehicleId());
    bookingRepository.save(existing);

    List<String> selected = List.copyOf(vehicleCandidate.availableCleanerIds().subList(0, professionalCount));
    for (var cleanerId : selected) {
      bookingCleanerRepository.save(new BookingCleanerEntity(existing, cleanerId));
    }

    bookingEventPublisher.publish(new BookingEventMessage(
            BookingEventType.BOOKING_RESCHEDULED,
            bookingId,
            existing.getVehicleId(),
            OffsetDateTime.of(existing.getStartAt(), ZoneOffset.UTC),
            OffsetDateTime.of(existing.getEndAt(), ZoneOffset.UTC),
            existing.getDurationHours(),
            selected,
            OffsetDateTime.now(ZoneOffset.UTC)
    ));

    return existing;
  }

}
