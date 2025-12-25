package com.electronics_store.impl;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.exception.RoleNotFoundException;
import com.electronics_store.model.Role;
import com.electronics_store.model.User;
import com.electronics_store.repository.RoleRepository;
import com.electronics_store.repository.UserRepository;
import com.electronics_store.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BindingResult bindingResult;
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User userReq;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        userReq = new User();
        userReq.setPassword("123456");
        userReq.setRoles(Collections.emptySet());

        roleUser = new Role();
        roleUser.setName("ROLE_USER");
    }

    @Nested
    class create {
        @Test
        void createUser_WhenUserHasNoRoles_ShouldAssignRoleUser() {
            // Given
            when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            User result = customUserDetailsService.create(userReq);

            // Then
            assertEquals("encodedPassword", result.getPassword());
            assertEquals(1, result.getRoles().size());
            assertTrue(result.getRoles().stream().anyMatch(r -> "ROLE_USER".equals(r.getName())));
            verify(roleRepository).findByName("ROLE_USER");
            verify(passwordEncoder).encode("123456");
            verify(userRepository).save(result);
        }

        @Test
        void createUser_WhenRoleUserDoesNotExist_ShouldThrowRoleNotFoundException() {
            // Given
            when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

            // When & Then
            RoleNotFoundException exception = assertThrows(RoleNotFoundException.class,
                    () -> customUserDetailsService.create(userReq));

            assertEquals("ROLE_USER not found", exception.getMessage());
            verify(passwordEncoder).encode("123456");
            verify(roleRepository).findByName("ROLE_USER");
            verify(userRepository, never()).save(any());
        }

        @Test
        void createUser_ShouldEncodeUserPasswordBeforeSaving() {
            // Given
            userReq.setRoles(Set.of(roleUser));
            when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            User result = customUserDetailsService.create(userReq);

            // Then
            assertEquals("encodedPassword", result.getPassword());
            verify(passwordEncoder).encode("123456");
            verify(userRepository).save(result);
            verify(roleRepository, never()).findByName(any());
        }

        @Test
        void createUser_WhenUserAlreadyHasRoles_ShouldNotChangeRoles() {
            // Given
            Role existingRole = new Role();
            existingRole.setName("ROLE_ADMIN");
            userReq.setRoles(Set.of(existingRole));
            when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            User result = customUserDetailsService.create(userReq);

            // Then
            assertEquals(1, result.getRoles().size());
            assertTrue(result.getRoles().contains(existingRole));
            verify(roleRepository, never()).findByName(any());
            verify(userRepository).save(result);
        }

        @Test
        void createUser_WhenUserIsValid_ShouldSaveAndReturnUser() {
            // Given
            Role someRole = new Role();
            someRole.setName("ROLE_CUSTOM");
            userReq.setRoles(Set.of(someRole));
            when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            User result = customUserDetailsService.create(userReq);

            // Then
            assertNotNull(result);
            assertEquals("encodedPassword", result.getPassword());
            assertEquals(1, result.getRoles().size());
            assertTrue(result.getRoles().contains(someRole));
            verify(passwordEncoder).encode("123456");
            verify(userRepository).save(result);
            verify(roleRepository, never()).findByName(any());
        }
    }

    @Nested
    class checkUniqueUsernameRegister{
        @Test
        void checkUniqueUsernameRegister_ShouldNotReject_WhenUsernameIsEmpty() {
            // Given
            UserDtoRegister dto = new UserDtoRegister();
            dto.setUsername("");

            // When
            customUserDetailsService.checkUniqueUsernameRegister(dto, bindingResult);

            // Then
            verify(userRepository, never()).existsByUsername(anyString());
            verify(bindingResult, never()).rejectValue(anyString(), anyString(), anyString());
        }

        @Test
        void checkUniqueUsernameRegister_ShouldNotReject_WhenUsernameDoesNotExist() {
            // Given
            UserDtoRegister dto = new UserDtoRegister();
            dto.setUsername("newuser");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);

            // When
            customUserDetailsService.checkUniqueUsernameRegister(dto, bindingResult);

            // Then
            verify(userRepository).existsByUsername("newuser");
            verify(bindingResult, never()).rejectValue(anyString(), anyString(), anyString());
        }

        @Test
        void checkUniqueUsernameRegister_ShouldReject_WhenUsernameExists() {
            // Given
            UserDtoRegister dto = new UserDtoRegister();
            dto.setUsername("existinguser");
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // When
            customUserDetailsService.checkUniqueUsernameRegister(dto, bindingResult);

            // Then
            verify(userRepository).existsByUsername("existinguser");
            verify(bindingResult).rejectValue("username", "username.exists", "Username already existed");
        }
    }
}
