package com.booking.dto;

import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import com.booking.model.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class Dtos {

    private Dtos() {
    }

    public record AuthRequest(
            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "L'email n'est pas valide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            @Size(min = 8, max = 72, message = "Le mot de passe doit faire entre 8 et 72 caractères")
            String password) {
    }

    public record TokenResponse(String token) {
    }

    public record ResourceRequest(
            @NotBlank(message = "Le nom est obligatoire")
            @Size(max = 100, message = "Le nom fait au plus 100 caractères")
            String name,

            @Size(max = 500, message = "La description fait au plus 500 caractères")
            String description) {
    }

    public record ResourceResponse(Long id, String name, String description) {

        public static ResourceResponse from(Resource resource) {
            return new ResourceResponse(resource.getId(), resource.getName(), resource.getDescription());
        }
    }

    public record ReservationRequest(
            @NotNull(message = "La machine est obligatoire")
            Long resourceId,

            @NotNull(message = "Le début est obligatoire")
            @Future(message = "Le début doit être dans le futur")
            LocalDateTime startTime,

            @NotNull(message = "La fin est obligatoire")
            LocalDateTime endTime) {
    }

    public record ReservationResponse(
            Long id,
            Long resourceId,
            String resourceName,
            String userEmail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status) {

        public static ReservationResponse from(Reservation r) {
            return new ReservationResponse(
                    r.getId(),
                    r.getResource().getId(),
                    r.getResource().getName(),
                    r.getUser().getEmail(),
                    r.getStartTime(),
                    r.getEndTime(),
                    r.getStatus());
        }
    }
}