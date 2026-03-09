package com.booking.bookingservice.integration.professionals.dto;

import java.io.Serializable;

public record CleanerDto(String id, String fullName, String vehicleId) implements Serializable {}
