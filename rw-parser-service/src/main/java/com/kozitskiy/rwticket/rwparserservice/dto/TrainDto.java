package com.kozitskiy.rwticket.rwparserservice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TrainDto(


        String trainNumber,
        String departureTime,
        String arrivalTime,
        String travelTime,
        List<TicketDto> tickets) {
    public  TrainDto{
        if(trainNumber == null || trainNumber.isBlank()){
            throw new IllegalArgumentException("Train number cannot be empty.");
        }
        if (departureTime == null || departureTime.isBlank()){
            throw new IllegalArgumentException("Departure time wan not found");
        }
        if (travelTime != null && !travelTime.contains("ч")){
            throw new IllegalArgumentException("Inappropriate travel time format");
        }
    }
}
