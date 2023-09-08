package ru.shutoff.messenger.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UrlTagValidator implements ConstraintValidator<UrlTagConstraint, String> {
	private static final String pattern = "[a-zA-Z0-9]+[^,.<>/?;:'\"\\[\\]{}\\\\|!@#$%^&*()\\-+=~`\\s]*[a-zA-Z0-9]";
	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		if (s == null) {
			return true;
		}
		return s.matches(pattern) && s.length() >= 5;
	}
}
