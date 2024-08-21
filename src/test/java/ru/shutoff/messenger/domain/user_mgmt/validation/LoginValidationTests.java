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
public class LoginValidationTests {
	@InjectMocks
	private LoginValidator loginValidator;

	@ParameterizedTest
	@MethodSource("provideInvalidData")
	public void isNotValid(String login) {
		assertFalse(loginValidator.isValid(login, null));
	}

	private static Stream<Arguments> provideInvalidData() {
		return Stream.of(
				Arguments.of("equality="),
				Arguments.of("plus+"),
				Arguments.of(""),
				Arguments.of(" "),
				Arguments.of("c,o,mm,a"),
				Arguments.of("co:l:on"),
				Arguments.of("semi;colon"),
				Arguments.of("vertical|line"),
				Arguments.of("back\\slash"),
				Arguments.of("forward/slash"),
				Arguments.of("[br]<ac>{ke}(ts)"),
				Arguments.of("using space"),
				Arguments.of("using'quotes"),
				Arguments.of("using\"double\"quotes"),
				Arguments.of("using`apostrophe"),
				Arguments.of("using~tilda"),
				Arguments.of("using!@#$%^&*()things"),
				Arguments.of("shrt"),
				Arguments.of("wrong_symbol_at_end+"),
				Arguments.of("+wrong_symbol_at_beginning"),
				Arguments.of("correct_symbol_at_end_"),
				Arguments.of("_correct_symbol_at_beginning")
		);
	}

	@ParameterizedTest
	@MethodSource("provideValidData")
	public void isValid(String login) {
		assertTrue(loginValidator.isValid(login, null));
	}

	private static Stream<Arguments> provideValidData() {
		return Stream.of(
				Arguments.of("use-dash"),
				Arguments.of("use_under_scope"),
				Arguments.of("use.dot"),
				Arguments.of("ALLCAPS"),
				Arguments.of("camelCase"),
				Arguments.of("snakecase"),
				Arguments.of("012digits_on_beginning"),
				Arguments.of("digits_on_end789"),
				Arguments.of("digits_3in4_5the6_middle"),
				Arguments.of("d1g1t5"),
				Arguments.of("kon1ch1wa1488"),
				Arguments.of("use_ALL-a110W3d-symBoLS")
		);
	}
}
