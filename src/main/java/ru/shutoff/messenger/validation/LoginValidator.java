package ru.shutoff.messenger.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class LoginValidator implements ConstraintValidator<LoginConstraint, String> {
	private static final String pattern = "[a-zA-Z0-9]+[^,<>/?;:'\"\\[\\]{}\\\\|!@#$%^&*()+=~`\\s]*[a-zA-Z0-9]";
	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		return s != null && s.matches(pattern) && s.length() >= 5;
	}
}
