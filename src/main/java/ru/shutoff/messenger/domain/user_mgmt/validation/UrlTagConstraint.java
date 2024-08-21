package ru.shutoff.messenger.domain.user_mgmt.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = UrlTagValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UrlTagConstraint {
	String message() default "Invalid url tag. Does not match pattern";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
