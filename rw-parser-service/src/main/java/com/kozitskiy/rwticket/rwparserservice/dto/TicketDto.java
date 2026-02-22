package com.kozitskiy.rwticket.rwparserservice.dto;

import lombok.Builder;

@Builder
public record TicketDto(String type,
                        int availableSeats,
                        String price)
{ }
