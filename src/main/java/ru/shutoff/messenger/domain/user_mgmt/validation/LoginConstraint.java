package ru.shutoff.messenger.domain.user_mgmt.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = LoginValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LoginConstraint {
	String message() default "Invalid login. Does not match pattern";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
