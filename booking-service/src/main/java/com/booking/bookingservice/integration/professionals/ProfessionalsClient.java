package com.booking.bookingservice.integration.professionals;

import com.booking.bookingservice.integration.professionals.dto.VehicleDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
    name = "professionals-service",
    path = "/api/v1"
)
public interface ProfessionalsClient {

  @CircuitBreaker(name = "professionals")
  @GetMapping("/vehicles")
  List<VehicleDto> listVehicles();

}
