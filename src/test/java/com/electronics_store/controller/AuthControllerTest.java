package com.electronics_store.controller;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.exception.RoleNotFoundException;
import com.electronics_store.mapper.UserMapper;
import com.electronics_store.model.User;
import com.electronics_store.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ExtendWith(SpringExtension.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private UserMapper userMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(userDetailsService, userMapper);
    }

    @Nested
    class register {
        @Test
        void register_WhenRoleUserNotFound_ShouldRedirectWithFailMessage() throws Exception {

            when(userMapper.toEntity(any(UserDtoRegister.class)))
                    .thenReturn(new User());

            doThrow(new RoleNotFoundException("ROLE_USER not found"))
                    .when(userDetailsService)
                    .create(any(User.class));

            mockMvc.perform(post("/auth")
                            .param("username", "minhquan")
                            .param("fullname", "Minh Quan")
                            .param("password", "12345678")
                            .param("confirmPassword", "12345678"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/register"))
                    .andExpect(flash().attribute("message", "Register failed!!"))
                    .andExpect(flash().attribute("status", "warning"));
        }

        @Test
        void register_WhenValidationError_ShouldReturnRegisterForm() throws Exception {
            mockMvc.perform(post("/auth")
                            .param("username", "")
                            .param("fullname", "")
                            .param("password", "123456")
                            .param("confirmPassword", "12345678"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register-form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors(
                            "userDTO",
                            "username",
                            "fullname",
                            "password",
                            "confirmPassword"
                    ));

            verify(userDetailsService, never()).checkUniqueUsernameRegister(any(), any());
            verify(userDetailsService, never()).create(any());
        }

        @Test
        void register_WhenDuplicateUsernameError_ShouldReturnRegisterForm() throws Exception {
            doAnswer(invocation -> {
                BindingResult br = invocation.getArgument(1);
                br.rejectValue(
                        "username",
                        "duplicate",
                        "Username already exists"
                );
                return null;
            }).when(userDetailsService)
                    .checkUniqueUsernameRegister(any(UserDtoRegister.class), any(BindingResult.class));

            mockMvc.perform(post("/auth")
                            .param("username", "admin123") // username đã tồn tại
                            .param("fullname", "Minh Quan")
                            .param("password", "12345678")
                            .param("confirmPassword", "12345678"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register-form"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors(
                            "userDTO",
                            "username"
                    ));

            verify(userDetailsService, never()).create(any());
        }

        @Test
        void register_WhenRegisterSuccessfully_ShouldReturnRegisterForm() throws Exception {

            when(userMapper.toEntity(any(UserDtoRegister.class)))
                    .thenReturn(new User());

            when(userDetailsService.create(any(User.class)))
                    .thenReturn(new User());

            mockMvc.perform(post("/auth")
                            .param("username", "minhquan")
                            .param("fullname", "Minh Quan")
                            .param("password", "12345678")
                            .param("confirmPassword", "12345678"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register-form"))
                    .andExpect(model().attributeExists("userDTO"))
                    .andExpect(model().attributeExists("message"))
                    .andExpect(model().attributeExists("status"));
        }
    }

    @Test
    void showLogin_WhenGetLoginForm_ShouldReturnLoginForm() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login-form"));
    }

    @Test
    void showRegister_WhenGetRegisterForm_ShouldReturnRegisterForm() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-form"))
                .andExpect(model().attributeExists("userDTO"));
    }
}