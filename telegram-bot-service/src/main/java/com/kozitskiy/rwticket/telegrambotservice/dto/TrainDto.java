package com.kozitskiy.rwticket.telegrambotservice.dto;

import java.util.List;

public record TrainDto(
        String trainNumber,
        String departureTime,
        String arrivalTime,
        String travelTime,
        List<TicketDto> tickets
) {
}
