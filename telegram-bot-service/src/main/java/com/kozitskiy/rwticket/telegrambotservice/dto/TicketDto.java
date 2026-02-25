package com.kozitskiy.rwticket.telegrambotservice.dto;

public record TicketDto(
        String type,
        int availableSeats,
        String price
) {
}
