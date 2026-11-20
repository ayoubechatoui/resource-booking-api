package com.booking.controller;

import com.booking.dto.Dtos.ResourceRequest;
import com.booking.dto.Dtos.ResourceResponse;
import com.booking.model.Resource;
import com.booking.repository.ResourceRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceRepository resourceRepository;

    public ResourceController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping
    public List<ResourceResponse> list() {
        return resourceRepository.findAll(Sort.by("name")).stream()
                .map(ResourceResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse create(@Valid @RequestBody ResourceRequest request) {
        String name = request.name().trim();
        if (resourceRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette machine existe déjà");
        }
        Resource saved = resourceRepository.save(new Resource(name, request.description()));
        return ResourceResponse.from(saved);
    }
}