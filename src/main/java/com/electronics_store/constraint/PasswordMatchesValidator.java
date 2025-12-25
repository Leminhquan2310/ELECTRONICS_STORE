package com.electronics_store.constraint;

import com.electronics_store.dto.user.UserDtoRegister;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserDtoRegister> {
    @Override
    public boolean isValid(UserDtoRegister dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getConfirmPassword() == null) {
            return true; // @NotBlank xử lý
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            // ❗ Tắt message mặc định
            context.disableDefaultConstraintViolation();
            // ❗ Gắn lỗi vào FIELD confirmPassword
            context.buildConstraintViolationWithTemplate(
                            "Password and Confirm Password is not match"
                    )
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
