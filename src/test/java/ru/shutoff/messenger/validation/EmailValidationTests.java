package ru.shutoff.messenger.validation;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class EmailValidationTests {
	@InjectMocks
	private EmailValidator emailValidator;

	@ParameterizedTest
	@MethodSource("provideInvalidData")
	public void isInvalid(String email) {
		assertFalse(emailValidator.isValid(email, null));
	}

	private static Stream<Arguments> provideInvalidData() {
		return Stream.of(
				Arguments.of(".@dev.ru"),
				Arguments.of(".incorrect@dev.ru"),
				Arguments.of("incorrect.@dev.ru"),
				Arguments.of("incorrect,@dev.ru"),
				//Arguments.of("incorrect/@dev.ru"),
				Arguments.of("incorrect;@dev.ru"),
				Arguments.of("incorrect<@>dev.ru"),
				Arguments.of("incorrect[@dev].ru"),
				Arguments.of("incorrect(@dev.ru)"),
				//Arguments.of("incorrect@{dev}.ru"),
				//Arguments.of("not!correct@dev.ru"),
				Arguments.of("double@att@dev.ru"),
				//Arguments.of("not|correct@dev.ru"),
				Arguments.of("incorrect@dev.ru,org"),
				//Arguments.of("incorrect@dev/ru"),
				Arguments.of("incorrec@dev\\ru"),
				Arguments.of("incorrect..dev@dev.ru"),
				Arguments.of("incorrect@.ru"),
				//Arguments.of("incorrect@ru"),
				Arguments.of("incorrect@ru."),
				Arguments.of("incorrect@ru;"),
				Arguments.of("incorrect@ru:"),
				//Arguments.of("incorrect@ru'"),
				Arguments.of("incorrect@ru.net."),
				Arguments.of("incorrect"),
				Arguments.of("dev.ru"),
				Arguments.of("@"),
				//Arguments.of("incorrect@dev-ru"),
				Arguments.of("incorrect@ru.net.org.")
		);
	}

	@ParameterizedTest
	@MethodSource("provideValidData")
	public void isValid(String email) {
		assertTrue(emailValidator.isValid(email, null));
	}

	private static Stream<Arguments> provideValidData() {
		return Stream.of(
				Arguments.of("v.putin@mail.ru"),
				Arguments.of("correct@mail.ru"),
				Arguments.of("correct@dev.org.ru"),
				Arguments.of("v-putin@mail.ru"),
				Arguments.of("v-putin.almighty@mail.ru"),
				Arguments.of("v.putin@mail.ru"),
				Arguments.of("correct@inbox-net.ru")
		);
	}
}
