package com.kozitskiy.rwticket.rwparserservice.controller;

import com.kozitskiy.rwticket.rwparserservice.dto.TrainDto;
import com.kozitskiy.rwticket.rwparserservice.service.RailwayParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final RailwayParserService railwayParserService;

    @GetMapping
    public ResponseEntity<List<TrainDto>> getTickets(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String date){

        log.info("Incoming request API: {} -> {} to {}", from, to, date);

        try {
            if(from.isBlank() || to.isBlank() || date.isBlank()){
                return ResponseEntity.badRequest().build();
            }

            List<TrainDto> trains = railwayParserService.getAvailableTrains(from, to, date);

            if (trains.isEmpty()){
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(trains);

        }catch (Exception e){
            log.error("Critical error during request process", e);
            return ResponseEntity.internalServerError().build();
        }

    }


}
