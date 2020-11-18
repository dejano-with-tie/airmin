package com.example.airmin.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDto {
    private AirportDto departure;
    private AirportDto arrival;
    private double price;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WithTotal {
        private double totalPrice;
        private List<RouteDto> routes;
    }
}
