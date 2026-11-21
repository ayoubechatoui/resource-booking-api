package com.booking.controller;

import com.booking.dto.Dtos.ReservationRequest;
import com.booking.dto.Dtos.ReservationResponse;
import com.booking.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(Authentication authentication,
                                      @Valid @RequestBody ReservationRequest request) {
        return reservationService.create(authentication.getName(), request);
    }

    @GetMapping("/mine")
    public List<ReservationResponse> mine(Authentication authentication) {
        return reservationService.listMine(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> all() {
        return reservationService.listAll();
    }
}