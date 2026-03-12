package com.booking.bookingservice.controller;

import com.booking.bookingservice.model.dto.request.CreateBookingRequest;
import com.booking.bookingservice.model.dto.request.UpdateBookingRequest;
import com.booking.bookingservice.model.dto.response.BookingResponse;
import com.booking.bookingservice.model.entity.BookingEntity;
import com.booking.bookingservice.model.mapper.BookingMapper;
import com.booking.bookingservice.service.BookingAppService;
import com.booking.common.model.dto.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookings", description = "APIs for creating and updating bookings")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingAppService bookingAppService;
  private final BookingMapper bookingMapper;

  public BookingController(BookingAppService bookingAppService, BookingMapper bookingMapper) {
    this.bookingAppService = bookingAppService;
    this.bookingMapper = bookingMapper;
  }

  @Operation(
          summary = "Create booking",
          description = "Creates a booking and assigns 1..3 cleaners from the same vehicle."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "201",
                  description = "Booking created",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = CustomResponse.class)
                  )
          ),
          @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
          @ApiResponse(responseCode = "409", description = "Booking rule violation", content = @Content),
          @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
  })
  @PostMapping
  public CustomResponse<BookingResponse> create(@Valid @RequestBody CreateBookingRequest req) {
    BookingEntity createdbookingEntity = bookingAppService.create(req.startAt(), req.durationHours(), req.professionalCount());
    return CustomResponse.createdOf(bookingMapper.map(createdbookingEntity));
  }

  @Operation(
          summary = "Reschedule booking",
          description = "Updates the booking date/time and re-validates availability and constraints."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Booking updated",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = CustomResponse.class)
                  )
          ),
          @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
          @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content),
          @ApiResponse(responseCode = "409", description = "Booking rule violation", content = @Content),
          @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
  })
  @PutMapping("/{bookingId}")
  public CustomResponse<BookingResponse> update(
          @PathVariable String bookingId,
          @Valid @RequestBody UpdateBookingRequest req
  ) {
    BookingEntity updatedBookingEntity = bookingAppService.reschedule(bookingId, req.newStartAt(), req.newDurationHours());
    return CustomResponse.successOf(bookingMapper.map(updatedBookingEntity));
  }
}