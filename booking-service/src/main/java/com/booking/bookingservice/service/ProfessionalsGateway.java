package com.booking.bookingservice.service;

import com.booking.bookingservice.integration.professionals.ProfessionalsClient;
import com.booking.bookingservice.integration.professionals.dto.VehicleDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessionalsGateway {

    private final ProfessionalsClient professionalsClient;

    public ProfessionalsGateway(ProfessionalsClient professionalsClient) {
        this.professionalsClient = professionalsClient;
    }

    @Cacheable(cacheNames = "booking-vehicles", sync = true)
    @CircuitBreaker(name = "professionals", fallbackMethod = "listVehiclesFallback")
    public List<VehicleDto> listVehicles() {
        return professionalsClient.listVehicles();
    }

    public List<VehicleDto> listVehiclesFallback(Throwable throwable) {
        throw new IllegalStateException("professionals-service is unavailable", throwable);
    }

}
