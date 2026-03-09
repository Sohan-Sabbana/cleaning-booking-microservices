package com.booking.bookingservice.integration.professionals.dto;

import java.io.Serializable;
import java.util.List;

public record VehicleDto(String id, String code, String licensePlate, List<CleanerDto> cleaners) implements Serializable {}
