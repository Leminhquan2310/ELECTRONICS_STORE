package com.electronics_store.service.impl;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.exception.RoleNotFoundException;
import com.electronics_store.model.Role;
import com.electronics_store.model.User;
import com.electronics_store.repository.RoleRepository;
import com.electronics_store.repository.UserRepository;
import com.electronics_store.security.CustomUserDetails;
import com.electronics_store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        if (user.getRoles().isEmpty()) {
            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));
            user.setRoles(Collections.singleton(role));
        }

        return userRepository.save(user);
    }

    public User toEntity(UserDtoRegister userDTORegister){
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
}