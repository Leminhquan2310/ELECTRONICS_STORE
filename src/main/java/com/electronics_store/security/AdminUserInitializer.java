package com.electronics_store.security;

import com.electronics_store.model.Role;
import com.electronics_store.model.User;
import com.electronics_store.repository.RoleRepository;
import com.electronics_store.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class AdminUserInitializer {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdminUser() {

        return args -> {
            if (userRepository.findByUsername("admin123").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin123");
                admin.setFullname("admin123");
                admin.setPassword(passwordEncoder.encode("12345678"));
                Role role = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                admin.setRoles(Collections.singleton(role));

                userRepository.save(admin);
               log.info("Created Admin User: {}", admin);
            }
        };
    }

}
