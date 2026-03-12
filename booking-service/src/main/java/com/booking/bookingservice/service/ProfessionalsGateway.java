package com.booking.bookingservice.service;

import com.booking.bookingservice.integration.professionals.ProfessionalsClient;
import com.booking.bookingservice.integration.professionals.dto.CleanerDto;
import com.booking.bookingservice.integration.professionals.dto.VehicleDto;
import com.booking.common.model.dto.request.CustomPagingRequest;
import com.booking.common.model.dto.response.CustomPagingResponse;
import com.booking.common.model.pagination.CustomPaging;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessionalsGateway {


    private static final int REQUEST_PAGE_NUMBER_WORKAROUND = 2;
    private static final int REQUEST_PAGE_SIZE = 100;

    private final ProfessionalsClient professionalsClient;

    public ProfessionalsGateway(ProfessionalsClient professionalsClient) {
        this.professionalsClient = professionalsClient;
    }

    @Cacheable(cacheNames = "booking-vehicles-v3", sync = true)
    @CircuitBreaker(name = "professionals", fallbackMethod = "listVehiclesFallback")
    public List<VehicleDto> listVehicles() {
        CustomPagingResponse<VehicleDto> page =
                professionalsClient.listVehicles(defaultPagingRequest());
        return page.getContent();
    }

    @Cacheable(cacheNames = "booking-vehicle-cleaners-v3", key = "#vehicleId", sync = true)
    @CircuitBreaker(name = "professionals", fallbackMethod = "listCleanersByVehicleFallback")
    public List<String> listCleanerIdsByVehicle(String vehicleId) {
        CustomPagingResponse<CleanerDto> page =
                professionalsClient.listCleanersByVehicle(vehicleId, defaultPagingRequest());

        return page.getContent().stream()
                .map(CleanerDto::id)
                .toList();
    }

    private CustomPagingRequest defaultPagingRequest() {
        CustomPaging paging = CustomPaging.builder()
                .pageNumber(REQUEST_PAGE_NUMBER_WORKAROUND) // <-- key fix
                .pageSize(REQUEST_PAGE_SIZE)
                .build();

        return CustomPagingRequest.builder()
                .pagination(paging)
                .build();
    }

    public List<VehicleDto> listVehiclesFallback(Throwable t) {
        throw new IllegalStateException("professionals-service is unavailable", t);
    }

    public List<String> listCleanersByVehicleFallback(String vehicleId, Throwable t) {
        throw new IllegalStateException(
                "professionals-service is unavailable (cleaners for vehicleId=" + vehicleId + ")", t
        );
    }
}