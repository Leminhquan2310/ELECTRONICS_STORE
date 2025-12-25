package com.electronics_store.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {

    String message() default "Password và Confirm Password không khớp";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}