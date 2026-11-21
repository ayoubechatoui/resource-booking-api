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
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record TokenResponse(String token) {
    }

    public record ResourceRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description) {
    }

    public record ResourceResponse(Long id, String name, String description) {

        public static ResourceResponse from(Resource resource) {
            return new ResourceResponse(resource.getId(), resource.getName(), resource.getDescription());
        }
    }

    public record ReservationRequest(
            @NotNull Long resourceId,
            @NotNull @Future LocalDateTime startTime,
            @NotNull LocalDateTime endTime) {
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