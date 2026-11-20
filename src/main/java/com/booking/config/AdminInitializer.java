package com.booking.config;

import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email}") String adminEmail,
                            @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(new User(email, passwordEncoder.encode(adminPassword), Role.ADMIN));
        log.info("Compte administrateur créé : {}", email);
    }
}