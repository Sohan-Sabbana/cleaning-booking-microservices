package com.booking.bookingservice.model.mapper;

import com.booking.bookingservice.model.dto.response.BookingResponse;
import com.booking.bookingservice.model.entity.BookingEntity;
import com.booking.common.model.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper extends BaseMapper<BookingEntity, BookingResponse> {
}
