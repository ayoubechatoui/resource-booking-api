package com.booking.service;

import com.booking.dto.Dtos.ReservationRequest;
import com.booking.dto.Dtos.ReservationResponse;
import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import com.booking.model.Resource;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    // Les réservations qui occupent encore un créneau (règle R4)
    private static final List<ReservationStatus> ACTIVE =
            List.of(ReservationStatus.EN_ATTENTE, ReservationStatus.VALIDEE);

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse create(String email, ReservationRequest request) {
        // R3 : la fin doit être après le début
        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin doit être après le début");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur inconnu"));

        // Verrou sur la machine : deux demandes simultanées passent l'une après l'autre
        Resource resource = resourceRepository.findByIdForUpdate(request.resourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine introuvable"));

        // R4 : pas de chevauchement avec une réservation en attente ou validée
        if (reservationRepository.existsOverlap(
                resource.getId(), request.startTime(), request.endTime(), ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce créneau est déjà pris");
        }

        Reservation saved = reservationRepository.save(
                new Reservation(user, resource, request.startTime(), request.endTime()));
        return ReservationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listMine(String email) {
        return reservationRepository.findByUserEmailOrderByStartTimeDesc(email).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listAll() {
        return reservationRepository.findAllByOrderByStartTimeDesc().stream()
                .map(ReservationResponse::from)
                .toList();
    }
}