package com.booking.bookingservice.repository;

import com.booking.bookingservice.model.entity.BookingEntity;
import com.booking.bookingservice.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, String> {

  @Query("select b from BookingEntity b " +
         "join b.cleaners bc " +
         "where bc.cleanerId = :cleanerId " +
         "and b.status = :status " +
         "and b.startAt >= :from " +
         "and b.startAt < :to " +
         "order by b.startAt asc")
  List<BookingEntity> findActiveBookingsForCleanerWithin(
      String cleanerId,
      BookingStatus status,
      LocalDateTime from,
      LocalDateTime to
  );
}
