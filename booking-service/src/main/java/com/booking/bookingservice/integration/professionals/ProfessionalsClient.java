package com.booking.bookingservice.integration.professionals;

import com.booking.bookingservice.integration.professionals.dto.VehicleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
    name = "professionals-service",
    path = "/api/v1"
)
public interface ProfessionalsClient {

  @GetMapping("/vehicles")
  List<VehicleDto> listVehicles();

}
