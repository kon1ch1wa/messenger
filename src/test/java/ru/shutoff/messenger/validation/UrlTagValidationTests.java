package ru.shutoff.messenger.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UrlTagValidationTests {
	@InjectMocks
	private UrlTagValidator urlTagValidator;

	@ParameterizedTest
	@MethodSource("provideInvalidData")
	public void isNotValid(String urlTag) {
		assertFalse(urlTagValidator.isValid(urlTag, null));
	}

	private static Stream<Arguments> provideInvalidData() {
		return Stream.of(
				Arguments.of("equality="),
				Arguments.of("plus+"),
				Arguments.of("use-dash"),
				Arguments.of(""),
				Arguments.of(" "),
				Arguments.of("d.o.t"),
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
	public void isValid(String urlTag) {
		assertTrue(urlTagValidator.isValid(urlTag, null));
	}

	private static Stream<Arguments> provideValidData() {
		return Stream.of(
				Arguments.of("use_under_scope"),
				Arguments.of("ALLCAPS"),
				Arguments.of("camelCase"),
				Arguments.of("snakecase"),
				Arguments.of("012digits_on_beginning"),
				Arguments.of("digits_on_end789"),
				Arguments.of("digits_3in4_5the6_middle"),
				Arguments.of("d1g1t5"),
				Arguments.of("kon1ch1wa1488"),
				Arguments.of("use_ALL_a110W3d_symBoLS")
		);
	}

	@Test
	public void nullIsValid() {
		assertTrue(urlTagValidator.isValid(null, null));
	}
}
