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

    @Transactional
    public ReservationResponse validate(Long id) {
        return decide(id, ReservationStatus.VALIDEE);
    }

    @Transactional
    public ReservationResponse refuse(Long id) {
        return decide(id, ReservationStatus.REFUSEE);
    }

    @Transactional
    public void cancel(Long id, String email) {
        Reservation reservation = find(id);
        // R7 : on n'annule que ses propres réservations
        if (!reservation.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce n'est pas votre réservation");
        }
        if (!ACTIVE.contains(reservation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette réservation est déjà refusée ou annulée");
        }
        reservation.setStatus(ReservationStatus.ANNULEE);
    }

    // R6 : seule une réservation en attente peut être validée ou refusée
    private ReservationResponse decide(Long id, ReservationStatus newStatus) {
        Reservation reservation = find(id);
        if (reservation.getStatus() != ReservationStatus.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une réservation en attente peut être validée ou refusée");
        }
        reservation.setStatus(newStatus);
        return ReservationResponse.from(reservation);
    }

    private Reservation find(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
    }
}