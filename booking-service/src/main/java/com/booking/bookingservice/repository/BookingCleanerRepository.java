package com.booking.bookingservice.repository;

import com.booking.bookingservice.model.entity.BookingCleanerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingCleanerRepository extends JpaRepository<BookingCleanerEntity, String> {

  List<BookingCleanerEntity> findByBooking_Id(String bookingId);

  void deleteByBooking_Id(String bookingId);
}
