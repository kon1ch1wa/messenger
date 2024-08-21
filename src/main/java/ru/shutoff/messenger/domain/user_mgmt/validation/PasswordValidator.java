package ru.shutoff.messenger.domain.user_mgmt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
	private static final String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()\\\\-_+=\\[\\]{}|',.?])(?!.*[\"\\\\<>/])(.{6,})";
	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		return s != null && s.matches(pattern) && s.chars().distinct().count() >= 6;
	}
}
