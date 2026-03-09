package com.electronics_store.service.impl;

import com.electronics_store.dto.image.ImageUploadResult;
import com.electronics_store.dto.user.UpdateProfileRequest;
import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.dto.user.UserProfileResponse;
import com.electronics_store.exception.RoleNotFoundException;
import com.electronics_store.model.Role;
import com.electronics_store.model.User;
import com.electronics_store.repository.RoleRepository;
import com.electronics_store.repository.UserRepository;
import com.electronics_store.security.CustomUserDetails;
import com.electronics_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService, UserService {

    @Value("${AVATAR_DEFAULT_URL}")
    private String avatarDefaultUrl;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CloudinaryImageService cloudinaryService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username or email not found: " + username));

        return new CustomUserDetails(user);
    }

    @Override
    @Transactional
    public User create(Object userDtoRegister) {
        UserDtoRegister dto = (UserDtoRegister) userDtoRegister;

        // Kiểm tra password match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        User user = toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        user.setEmail(dto.getEmail());
        user.setStatus(User.UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setEmailVerificationToken(generateVerificationToken());
        user.setAvatarUrl(avatarDefaultUrl);

        if (user.getRoles().isEmpty()) {
            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));
            user.setRoles(Collections.singleton(role));
        }

        log.info("Password create: {}", user.getPassword());
        return userRepository.save(user);
    }

    public User toEntity(UserDtoRegister userDTORegister) {
        User user = new User();
        user.setUsername(userDTORegister.getUsername());
        user.setFullName(userDTORegister.getFullName());
        user.setPassword(userDTORegister.getPassword());
        return user;
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }


    @Override
    @Transactional
    public boolean delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public void checkUniqueUsernameRegister(UserDtoRegister userDTO, BindingResult bindingResult) {
        if (userDTO.getUsername() != null &&
                !userDTO.getUsername().isEmpty() &&
                userRepository.existsByUsername(userDTO.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Username already existed");
        }

        if (userDTO.getEmail() != null &&
                !userDTO.getEmail().isEmpty() &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email already existed");
        }

        if (userDTO.getPhone() != null &&
                !userDTO.getPhone().isEmpty() &&
                userRepository.existsByPhone(userDTO.getPhone())) {
            bindingResult.rejectValue("phone", "phone.exists", "Phone number already existed");
        }
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null ||
                !auth.isAuthenticated() ||
                auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUser();
    }

    @Override
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserProfileResponse(user);
    }

    /**
     * Lấy CustomUserDetails của user hiện tại
     */
    public CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null ||
                !auth.isAuthenticated() ||
                auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }

        return (CustomUserDetails) auth.getPrincipal();
    }


    /**
     * Lấy ID của user hiện tại
     */
    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    // Additional methods
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }

    @Override
    public void updateUserProfile(String username, UpdateProfileRequest dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        if (dto.getDateOfBirth() != null) {
            user.setDateOfBirth(dto.getDateOfBirth().atStartOfDay());
        } else {
            user.setDateOfBirth(null); // Cho phép lưu null vào database
        }
        user.setGender(dto.getGender());

        userRepository.save(user);
    }

    @Override
    public void updateAvatar(String username, MultipartFile file) {
        ImageUploadResult result = cloudinaryService.upload(file);
        User user = userRepository.findByUsername(username)
                .orElseThrow();
        if (!avatarDefaultUrl.equals(user.getAvatarUrl())) {
            String publicId = cloudinaryService.extractPublicId(user.getAvatarUrl());
            cloudinaryService.delete(publicId);
        }

        user.setAvatarUrl(result.getUrl());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Check current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 2. Check new password != old password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // 3. Encode & save
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Transactional
    public void updateLoginInfo(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    @Transactional
    public void incrementFailedLoginAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByEmailVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void generatePasswordResetToken(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordResetToken(generateVerificationToken());
            user.setPasswordResetExpires(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            // Send email with reset token
        });
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByPasswordResetToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPasswordResetExpires().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                user.setPasswordResetExpires(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void updateProfile(Long userId, String fullName, String phone, String address, String avatarUrl) {
        User user = getById(userId);
        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        if (address != null) user.setAddress(address);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = getById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}