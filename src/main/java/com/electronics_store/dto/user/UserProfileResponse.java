package com.electronics_store.dto.user;

import com.electronics_store.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private String address;

    private String avatarUrl;

    private LocalDateTime dateOfBirth;

    private User.Gender gender;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;
}
