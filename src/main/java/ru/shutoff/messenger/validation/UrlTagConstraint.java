package ru.shutoff.messenger.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UrlTagValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UrlTagConstraint {
	String message() default "Invalid url tag. Does not match pattern";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
