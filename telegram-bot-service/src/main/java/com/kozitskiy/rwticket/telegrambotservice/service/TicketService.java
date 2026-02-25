package com.kozitskiy.rwticket.telegrambotservice.service;

import com.kozitskiy.rwticket.telegrambotservice.dto.TicketDto;
import com.kozitskiy.rwticket.telegrambotservice.dto.TrainDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    @Value("${services.parser-url}")
    private String parseUrl;

    private final RestTemplate restTemplate;

    public String findTickets(String from, String to, String date){
        try {

            String urlTemplate = parseUrl + "?from={from}&to={to}&date={date}";

            TrainDto[] trains = restTemplate.getForObject(
                    urlTemplate,
                    TrainDto[].class,
                    from, to, date
            );

            if (trains == null || trains.length == 0){
                return "К сожалению, билеты на эту дату не найдены 😔";
            }
            return formatMessage(trains, from, to, date);

        } catch (Exception e) {
            log.error("Ошибка при поиске билетов", e);
            return "❌ Ошибка при поиске билетов. Возможно, сервис БЖД сейчас недоступен.";
        }
    }

    private String formatMessage(TrainDto[] trains, String from, String to, String date) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚂 **Билеты: ").append(from).append(" ➡️ ").append(to).append("**\n");
        sb.append("📅 Дата: ").append(date).append("\n\n");

        for (TrainDto train : trains) {
            sb.append("🚆 **Поезд:** ").append(train.trainNumber()).append("\n");
            sb.append("🕒 **Отправление:** ").append(train.departureTime()).append("\n");
            sb.append("🏁 **Прибытие:** ").append(train.arrivalTime()).append("\n");
            sb.append("⏳ **В пути:** ").append(train.travelTime()).append("\n");

            if (train.tickets() == null || train.tickets().isEmpty()) {
                sb.append("❌ Нет доступных мест\n");
            } else {
                sb.append("🎫 **Места:**\n");
                for (TicketDto ticket : train.tickets()) {
                    sb.append("  ▫️ ").append(ticket.type())
                            .append(" — ").append(ticket.availableSeats()).append(" шт. ")
                            .append("(").append(ticket.price()).append(")\n");
                }
            }
            sb.append("➖➖➖➖➖➖➖➖➖➖\n");
        }

        return sb.toString();
    }

}
