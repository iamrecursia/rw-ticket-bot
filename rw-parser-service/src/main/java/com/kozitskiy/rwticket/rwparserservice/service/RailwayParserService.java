package com.kozitskiy.rwticket.rwparserservice.service;

import com.kozitskiy.rwticket.rwparserservice.dto.TicketDto;
import com.kozitskiy.rwticket.rwparserservice.dto.TrainDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RailwayParserService {

    @Value("${rw.api.base-url}")
    private String baseUrl;

    public List<TrainDto> getAvailableTrains(String from, String to, String date){
        List<TrainDto> trains = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(baseUrl)
                    .data("from", from)
                    .data("from_exp", "2100000")
                    .data("to", to)
                    .data("to_exp", "2100180")
                    .data("date", date)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .get();

            Elements trainCards = doc.select(".sch-table__row");

            if (trainCards.isEmpty()) {
                log.error("There is no available train! Page response: {}", doc.body().text());
                return trains;
            }

            for (Element card: trainCards){
                try {
                    String trainNumber = card.attr("data-train-number").trim();

                    if (trainNumber.isEmpty()) {
                        continue;
                    }

                    String depTime = card.select("[data-sort=departure]").text().trim();
                    String arrTime = card.select("[data-sort=arrival]").text().trim();
                    String travelTime = card.select("[data-sort=duration]").text().trim();

                    if (travelTime.isEmpty()) {
                        travelTime = card.select(".duration").text().trim();
                    }

                    List<TicketDto> trainTickets = new ArrayList<>();

                    Elements ticketItems = card.select(".sch-table__t-item.has-quant");
                    String lastKnownType = "Общий";

                    for (Element item: ticketItems){
                        String carType = item.select(".sch-table__t-name").text().trim();

                        if (carType.isEmpty()) {
                            carType = lastKnownType;
                        } else {
                            lastKnownType = carType;
                        }

                        String seatsStr = item.select(".sch-table__t-quant span").text().trim();
                        int availableSeats = seatsStr.isEmpty() ? 0 : Integer.parseInt(seatsStr);

                        String price = item.select(".js-price").text().trim();

                        trainTickets.add(TicketDto.builder()
                                .type(carType)
                                .availableSeats(availableSeats)
                                .price(price)
                                .build());
                    }

                    TrainDto train = TrainDto.builder()
                            .trainNumber(trainNumber)
                            .departureTime(depTime)
                            .arrivalTime(arrTime)
                            .travelTime(travelTime)
                            .tickets(trainTickets)
                            .build();

                    trains.add(train);

                } catch (Exception e) {
                    log.warn("Create dto error: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error connecting to BRW: {}", e.getMessage(), e);
        }

        return trains;
    }

}
