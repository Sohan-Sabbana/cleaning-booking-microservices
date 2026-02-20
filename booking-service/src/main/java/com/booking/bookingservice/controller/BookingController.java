package com.booking.bookingservice.controller;

import com.booking.bookingservice.model.dto.request.CreateBookingRequest;
import com.booking.bookingservice.model.dto.request.UpdateBookingRequest;
import com.booking.bookingservice.model.dto.response.BookingResponse;
import com.booking.bookingservice.model.mapper.BookingMapper;
import com.booking.bookingservice.service.BookingAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookings")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingAppService bookingAppService;
  private final BookingMapper bookingMapper;

  public BookingController(BookingAppService bookingAppService, BookingMapper bookingMapper) {
    this.bookingAppService = bookingAppService;
    this.bookingMapper = bookingMapper;
  }

  @PostMapping
  public BookingResponse create(@Valid @RequestBody CreateBookingRequest req) {
    var b = bookingAppService.create(req.startAt(), req.durationHours(), req.professionalCount());
    return bookingMapper.map(b);
  }

  @PutMapping("/{bookingId}")
  public BookingResponse update(@PathVariable String bookingId, @Valid @RequestBody UpdateBookingRequest req) {
    var b = bookingAppService.reschedule(bookingId, req.newStartAt(), req.newDurationHours());
    return bookingMapper.map(b);
  }
}
