package com.booking.dto;

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
}