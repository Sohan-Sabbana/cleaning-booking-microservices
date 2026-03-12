package com.booking.bookingservice.service;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        this.policyChain = new PolicyChain(List.of(
                new NotFridayPolicy(),
                new WorkingHoursPolicy(),
                new DurationPolicy()
        ));
    }

    public List<VehicleDto> vehicles() {
        return professionalsGateway.listVehicles();
    }

    @Cacheable(cacheNames = "booking-availability-by-date-v4", keyGenerator = "bookingKeyGenerator")
    public Map<String, Map<String, Map<Integer, List<LocalTime>>>> availabilityByDate(LocalDate date) {

        Map<String, Map<String, Map<Integer, List<LocalTime>>>> result =
                new LinkedHashMap<String, Map<String, Map<Integer, List<LocalTime>>>>();

        LocalDateTime dayFrom = date.atStartOfDay();
        LocalDateTime dayTo = date.plusDays(1).atStartOfDay();

        List<VehicleDto> vehicles = professionalsGateway.listVehicles();
        for (VehicleDto vehicle : vehicles) {

            Map<String, Map<Integer, List<LocalTime>>> byCleaner =
                    new LinkedHashMap<String, Map<Integer, List<LocalTime>>>();

            List<String> cleanerIds = professionalsGateway.listCleanerIdsByVehicle(vehicle.id());
            for (String cleanerId : cleanerIds) {

                List<BookingEntity> existing =
                        bookingRepository.findActiveBookingsForCleanerWithin(
                                cleanerId, BookingStatus.ACTIVE, dayFrom, dayTo
                        );

                List<LocalTime> startTimes2h = computeStartTimes(date, 2, existing);
                List<LocalTime> startTimes4h = computeStartTimes(date, 4, existing);

                byCleaner.put(cleanerId, Map.of(
                        2, startTimes2h,
                        4, startTimes4h
                ));
            }

            result.put(vehicle.id(), byCleaner);
        }

        return result;
    }

    @Cacheable(cacheNames = "booking-availability-slot-v4", keyGenerator = "bookingKeyGenerator")
    public List<String> availableCleanersFor(String vehicleId, LocalDateTime startAt, int durationHours) {
        return availableCleanersFor(vehicleId, startAt, durationHours, null);
    }

    public List<String> availableCleanersFor(
            String vehicleId,
            LocalDateTime startAt,
            int durationHours,
            String excludeBookingId
    ) {
        LocalDateTime endAt = startAt.plusHours(durationHours);
        policyChain.validate(startAt, endAt, durationHours);

        List<VehicleDto> vehicles = professionalsGateway.listVehicles();
        boolean vehicleExists = false;

        for (VehicleDto v : vehicles) {
            if (v.id().equals(vehicleId)) {
                vehicleExists = true;
                break;
            }
        }

        if (!vehicleExists) {
            throw new IllegalArgumentException("Unknown vehicleId: " + vehicleId);
        }

        LocalDateTime from = startAt.toLocalDate().atStartOfDay();
        LocalDateTime to = startAt.toLocalDate().plusDays(1).atStartOfDay();

        List<String> cleanerIds = professionalsGateway.listCleanerIdsByVehicle(vehicleId);

        List<String> available = new ArrayList<String>();
        for (String cleanerId : cleanerIds) {

            List<BookingEntity> existing;
            if (excludeBookingId == null) {
                existing = bookingRepository.findActiveBookingsForCleanerWithin(
                        cleanerId, BookingStatus.ACTIVE, from, to
                );
            } else {
                existing = bookingRepository.findActiveBookingsForCleanerWithinExcludingBooking(
                        cleanerId, BookingStatus.ACTIVE, from, to, excludeBookingId
                );
            }

            if (ScheduleRules.isAvailable(startAt, endAt, existing)) {
                available.add(cleanerId);
            }
        }

        return available;
    }

    public Optional<VehicleCandidate> findVehicleWithCapacity(LocalDateTime startAt, int durationHours, int professionalCount) {
        return findVehicleWithCapacity(startAt, durationHours, professionalCount, null);
    }

    /**
     * Finds any vehicle that can serve professionalCount cleaners for given slot.
     * If excludeBookingId is provided, conflicts ignore that booking.
     */
    public Optional<VehicleCandidate> findVehicleWithCapacity(
            LocalDateTime startAt,
            int durationHours,
            int professionalCount,
            String excludeBookingId
    ) {
        LocalDateTime endAt = startAt.plusHours(durationHours);
        policyChain.validate(startAt, endAt, durationHours);

        List<VehicleDto> vehicles = professionalsGateway.listVehicles();
        for (VehicleDto vehicle : vehicles) {
            List<String> availableCleaners =
                    availableCleanersFor(vehicle.id(), startAt, durationHours, excludeBookingId);

            if (availableCleaners.size() >= professionalCount) {
                return Optional.of(new VehicleCandidate(vehicle.id(), availableCleaners));
            }
        }

        return Optional.empty();
    }

    private List<LocalTime> computeStartTimes(LocalDate date, int durationHours, List<BookingEntity> existing) {

        LocalDateTime startAt = date.atTime(START);
        LocalDateTime lastStart = date.atTime(END).minusHours(durationHours);

        List<LocalTime> times = new ArrayList<LocalTime>();

        LocalDateTime t = startAt;
        while (!t.isAfter(lastStart)) {

            LocalDateTime end = t.plusHours(durationHours);

            // You can keep this validate; it should not throw for your allowed durations
            // and working-hours windows, but it is safe to enforce domain constraints here.
            policyChain.validate(t, end, durationHours);

            if (ScheduleRules.isAvailable(t, end, existing)) {
                times.add(t.toLocalTime());
            }

            t = t.plusMinutes(STEP_MINUTES);
        }

        return times;
    }

    public record VehicleCandidate(String vehicleId, List<String> availableCleanerIds) {}
}