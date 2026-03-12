package com.booking.bookingservice.integration.professionals;

import com.booking.bookingservice.integration.professionals.dto.CleanerDto;
import com.booking.bookingservice.integration.professionals.dto.VehicleDto;
import com.booking.common.model.dto.request.CustomPagingRequest;
import com.booking.common.model.dto.response.CustomPagingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "professionals-service",
        path = "/api/v1"
)
public interface ProfessionalsClient {

  @PostMapping("/vehicles")
  CustomPagingResponse<VehicleDto> listVehicles(@RequestBody CustomPagingRequest pagingRequest);

  @PostMapping("/vehicles/{vehicleId}/cleaners")
  CustomPagingResponse<CleanerDto> listCleanersByVehicle(
          @PathVariable String vehicleId,
          @RequestBody CustomPagingRequest pagingRequest
  );
}
