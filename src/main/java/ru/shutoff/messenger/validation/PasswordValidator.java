package ru.shutoff.messenger.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
	private static final String pattern = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()\\-_+=\\[\\]{}|',.<>/?])(?!.*[\"\\\\])(.{6,})";
	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		return s != null && s.matches(pattern) && s.chars().distinct().count() >= 6;
	}
}
