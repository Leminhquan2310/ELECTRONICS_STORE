package com.electronics_store.constraint;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.electronics_store.dto.user.UserDtoRegister;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PasswordMatchesValidatorTest {

    @InjectMocks
    private PasswordMatchesValidator validator;
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builderStep1;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext builderStep2;

    @Test
    void isValid_ShouldReturnTrue_WhenPasswordOrConfirmPasswordIsNull() {
        UserDtoRegister dto = new UserDtoRegister();
        dto.setPassword(null);
        dto.setConfirmPassword(null);

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void isValid_ShouldReturnFalseAndAddViolation_WhenPasswordDoesNotMatchConfirmPassword() {
        UserDtoRegister dto = new UserDtoRegister();
        dto.setPassword("123456");
        dto.setConfirmPassword("abcdef");

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builderStep1);
        when(builderStep1.addPropertyNode(anyString())).thenReturn(builderStep2);
        when(builderStep2.addConstraintViolation()).thenReturn(context);

        boolean result = validator.isValid(dto, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Password and Confirm Password is not match");
        verify(builderStep1).addPropertyNode("confirmPassword");
        verify(builderStep2).addConstraintViolation();
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPasswordMatchesConfirmPassword() {
        UserDtoRegister dto = new UserDtoRegister();
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }
}
