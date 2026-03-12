package com.booking.bookingservice.repository;

import com.booking.bookingservice.model.entity.BookingCleanerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingCleanerRepository extends JpaRepository<BookingCleanerEntity, String> {

  List<BookingCleanerEntity> findByBooking_Id(String bookingId);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from BookingCleanerEntity bc where bc.booking.id = :bookingId")
  void deleteAllByBookingId(String bookingId);
}
