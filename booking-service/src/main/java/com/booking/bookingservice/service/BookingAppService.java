package com.booking.bookingservice.service;

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
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingAppService {

  private final BookingRepository bookingRepository;
  private final BookingCleanerRepository bookingCleanerRepository;
  private final AvailabilityService availabilityService;
  private final BookingEventPublisher bookingEventPublisher;

  public BookingAppService(
          BookingRepository bookingRepository,
          BookingCleanerRepository bookingCleanerRepository,
          AvailabilityService availabilityService,
          BookingEventPublisher bookingEventPublisher
  ) {
    this.bookingRepository = bookingRepository;
    this.bookingCleanerRepository = bookingCleanerRepository;
    this.availabilityService = availabilityService;
    this.bookingEventPublisher = bookingEventPublisher;
  }

  @Transactional
  @Caching(evict = {
          @CacheEvict(cacheNames = "booking-availability-by-date-v4", allEntries = true),
          @CacheEvict(cacheNames = "booking-availability-slot-v4", allEntries = true)
  })
  public BookingEntity create(LocalDateTime startAt, int durationHours, int professionalCount) {
    if (professionalCount < 1 || professionalCount > 3) {
      throw new DomainException("Cleaner professional count must be 1, 2, or 3.");
    }

    AvailabilityService.VehicleCandidate vehicleCandidate =
            availabilityService.findVehicleWithCapacity(startAt, durationHours, professionalCount)
                    .orElseThrow(() -> new DomainException("No available vehicle/cleaners for the requested time window."));

    LocalDateTime endAt = startAt.plusHours(durationHours);

    BookingEntity booking = new BookingEntity(
            startAt,
            endAt,
            durationHours,
            vehicleCandidate.vehicleId(),
            BookingStatus.ACTIVE
    );

    bookingRepository.save(booking);

    String bookingId = booking.getId();

    List<String> selected = vehicleCandidate.availableCleanerIds().stream()
            .distinct()
            .limit(professionalCount)
            .toList();

    if (selected.size() < professionalCount) {
      throw new DomainException("Not enough available cleaners.");
    }

    bookingCleanerRepository.saveAll(
            selected.stream().map(cid -> new BookingCleanerEntity(booking, cid)).toList()
    );

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
  @Caching(evict = {
          @CacheEvict(cacheNames = "booking-availability-by-date-v4", allEntries = true),
          @CacheEvict(cacheNames = "booking-availability-slot-v4", allEntries = true)
  })
  public BookingEntity reschedule(String bookingId, LocalDateTime newStartAt, int newDurationHours) {

    BookingEntity existing = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new DomainException("Booking not found: " + bookingId));

    List<BookingCleanerEntity> previousAssignments = bookingCleanerRepository.findByBooking_Id(bookingId);
    int professionalCount = previousAssignments.size();

    List<String> previousCleanerIds = previousAssignments.stream()
            .map(BookingCleanerEntity::getCleanerId)
            .distinct()
            .toList();

    String chosenVehicleId = existing.getVehicleId();
    List<String> chosenCleanerIds;

    // Try keep same vehicle + same cleaners (excluding THIS booking)
    List<String> availableSameVehicle = availabilityService.availableCleanersFor(
            chosenVehicleId,
            newStartAt,
            newDurationHours,
            bookingId
    );

    if (new HashSet<>(availableSameVehicle).containsAll(previousCleanerIds)) {
      chosenCleanerIds = previousCleanerIds;
    } else {
      AvailabilityService.VehicleCandidate vehicleCandidate = availabilityService.findVehicleWithCapacity(
              newStartAt,
              newDurationHours,
              professionalCount,
              bookingId
      ).orElseThrow(() -> new DomainException("No availability to reschedule for the requested time window."));

      chosenVehicleId = vehicleCandidate.vehicleId();
      chosenCleanerIds = vehicleCandidate.availableCleanerIds().stream()
              .distinct()
              .limit(professionalCount)
              .toList();

      if (chosenCleanerIds.size() < professionalCount) {
        throw new DomainException("Not enough available cleaners to reschedule.");
      }
    }

    bookingCleanerRepository.deleteAllByBookingId(bookingId);

    LocalDateTime newEndAt = newStartAt.plusHours(newDurationHours);
    existing.reschedule(newStartAt, newEndAt, newDurationHours, chosenVehicleId);
    bookingRepository.save(existing);

    bookingCleanerRepository.saveAll(
            chosenCleanerIds.stream()
                    .map(cid -> new BookingCleanerEntity(existing, cid))
                    .toList()
    );

    bookingEventPublisher.publish(new BookingEventMessage(
            BookingEventType.BOOKING_RESCHEDULED,
            bookingId,
            existing.getVehicleId(),
            OffsetDateTime.of(existing.getStartAt(), ZoneOffset.UTC),
            OffsetDateTime.of(existing.getEndAt(), ZoneOffset.UTC),
            existing.getDurationHours(),
            chosenCleanerIds,
            OffsetDateTime.now(ZoneOffset.UTC)
    ));

    return existing;
  }

}