package com.electronics_store.dto.user;

import com.electronics_store.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không quá 100 ký tự")
    private String email;

    @Pattern(
            regexp = "^(\\+84|0)[0-9]{9,10}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;

    @Size(max = 500, message = "Địa chỉ không quá 500 ký tự")
    private String address;

    private LocalDate dateOfBirth;

    private User.Gender gender;

    @Size(max = 500, message = "Avatar URL không quá 500 ký tự")
    private String avatarUrl;
}