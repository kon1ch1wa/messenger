package ru.shutoff.messenger.domain.user_mgmt.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PhoneNumberValidationTests {
	@InjectMocks
	private PhoneNumberValidator phoneNumberValidator;

	@ParameterizedTest
	@MethodSource("provideInvalidData")
	public void isInvalid(String phoneNumber) {
		assertFalse(phoneNumberValidator.isValid(phoneNumber, null));
	}

	private static Stream<Arguments> provideInvalidData() {
		return Stream.of(
				Arguments.of(""),
				Arguments.of("81119999999"),
				Arguments.of("71119999999"),
				Arguments.of("+71119999999"),
				Arguments.of(" "),
				Arguments.of("+372 600 1247"),
				Arguments.of("+3726001247"),
				Arguments.of("+37260001247"),
				Arguments.of("+372587345"),
				Arguments.of("79015919044"),
				Arguments.of( "79015919014    "),
				Arguments.of("   79015619014"),
				Arguments.of("   79015619014    "),
				Arguments.of("-79999999999"),
				Arguments.of("+69999999999"),
				Arguments.of("+779999999999"),
				Arguments.of("+7999999 999"),
				Arguments.of("+7+999-999-99-99"),
				Arguments.of("+7-999+999-99-99"),
				Arguments.of("+7-999-999+99-99"),
				Arguments.of("+7-999-999-99+99")
		);
	}

	@ParameterizedTest
	@MethodSource("provideValidData")
	public void isValid(String phoneNumber) {
		assertTrue(phoneNumberValidator.isValid(phoneNumber, null));
	}

	private static Stream<Arguments> provideValidData() {
		return Stream.of(
				Arguments.of("89015919046"),
				Arguments.of("89999999999"),
				Arguments.of("+79015919045"),
				Arguments.of("+79999999999"),
				Arguments.of("+79015919044"),
				Arguments.of( "+79015919014    "),
				Arguments.of("   +79015619014"),
				Arguments.of("   +79015619014    "),
				Arguments.of("+7999999 9999"),
				Arguments.of("+7-999-999-99-99"),
				Arguments.of("+7(999)999 9999"),
				Arguments.of("+7(999)99999 99"),
				Arguments.of("+7(999)999-99-99"),
				Arguments.of("+7 999 999 99 99")
		);
	}
}
