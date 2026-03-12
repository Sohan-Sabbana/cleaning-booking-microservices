package com.booking.bookingservice.config;

import com.booking.common.model.dto.request.CustomPagingRequest;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class CacheKeyGeneratorConfig {

    /**
     * Generic key generator (same style as professionals-service)
     * - prefixes method name
     * - appends each parameter in a stable way
     */
    @Bean("bookingKeyGenerator")
    public KeyGenerator bookingKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder key = new StringBuilder(method.getName());

                for (Object param : params) {
                    key.append("::").append(toKeyPart(param));
                }

                return key.toString();
            }

            private String toKeyPart(Object param) {
                if (param == null) return "null";

                if (param instanceof String s) return s;
                if (param instanceof Integer i) return i.toString();
                if (param instanceof Long l) return l.toString();

                if (param instanceof LocalDate d) return d.toString();
                if (param instanceof LocalTime t) return t.toString();
                if (param instanceof LocalDateTime dt) return dt.toString();

                // Useful if you later add cacheable methods with paging requests in booking-service
                if (param instanceof CustomPagingRequest pagingRequest) {
                    Integer pageNumber = pagingRequest.getPagination() != null
                            ? pagingRequest.getPagination().getPageNumber()
                            : null;

                    Integer pageSize = pagingRequest.getPagination() != null
                            ? pagingRequest.getPagination().getPageSize()
                            : null;

                    String sortBy = pagingRequest.getSorting() != null
                            ? pagingRequest.getSorting().getSortBy()
                            : "";

                    String sortDirection = pagingRequest.getSorting() != null
                            ? pagingRequest.getSorting().getSortDirection()
                            : "";

                    return "page=" + pageNumber
                            + ",size=" + pageSize
                            + ",sortBy=" + sortBy
                            + ",sortDir=" + sortDirection;
                }

                // Arrays
                if (param.getClass().isArray()) {
                    int len = java.lang.reflect.Array.getLength(param);
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < len; i++) {
                        if (i > 0) sb.append(",");
                        sb.append(toKeyPart(java.lang.reflect.Array.get(param, i)));
                    }
                    sb.append("]");
                    return sb.toString();
                }

                return String.valueOf(param);
            }
        };
    }
}