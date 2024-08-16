package ru.shutoff.messenger.validation;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PasswordValidationTests {
	@InjectMocks
	private PasswordValidator passwordValidator;

	@ParameterizedTest
	@MethodSource("provideInvalidData")
	public void isInvalid(String password) {
		assertFalse(passwordValidator.isValid(password, null));
	}

	private static Stream<Arguments> provideInvalidData() {
		return Stream.of(
				Arguments.of(""),
				Arguments.of(" "),
				Arguments.of("\""),
				Arguments.of("1"),
				Arguments.of("!"),
				Arguments.of("a"),
				Arguments.of("A"),
				Arguments.of("ONLYCAPS"),
				Arguments.of("0NLYCAP5W1THD1G1T5"),
				Arguments.of("ONLY_CAPS_WITH_SPEC_SYMBOL"),
				Arguments.of("0NLY_CAP5.W1TH_D1G1T5.AND_5P3C_5YMB075"),
				Arguments.of("onlylow"),
				Arguments.of("0n7y70ww1thd1g1t5"),
				Arguments.of("only_low_with_spec_symbol"),
				Arguments.of("0n7y_70w.w1th_d1g1t5.and_5p3c_5ymb075"),
				Arguments.of("NoSpecialSymbolsAndDigits"),
				Arguments.of("N05p3c1a75ymb075ButDigits"),
				Arguments.of("12.3_4.5.6_7.89"),
				Arguments.of("OOoo0!"),
				Arguments.of("BackS1ash_At_the_END\\"),
				Arguments.of("\"Pa55w0rd_W1th.D1G1T5.AND.5p3c_5ymb075\"")
		);
	}

	@ParameterizedTest
	@MethodSource("provideValidData")
	public void isValid(String password) {
		assertTrue(passwordValidator.isValid(password, null));
	}

	private static Stream<Arguments> provideValidData() {
		return Stream.of(
				Arguments.of("Pa55w0rd_W1th.D1G1T5.AND.5p3c_5ymb075"),
				Arguments.of("Pa55w0rd_W1th.D1G1T5{AND}5p3c_5ymb075"),
				Arguments.of("Pa55w0rd_W1th.D1G1T5.AND<5p3c_5ymb075>"),
				Arguments.of("Pa55w0rd_W1th[D1G1T5]AND.5p3c_5ymb075"),
				Arguments.of("(Pa55w0rd_W1th)D1G1T5.AND.5p3c_5ymb075"),
				Arguments.of("Pa55w0rd_W1th-D1G1T5-AND-5p3c_5ymb075"),
				Arguments.of("-=Pa55w0rd_W1th=D1G1T5=AND=5p3c_5ymb075=-"),
				Arguments.of("^*P@55w0rd_W1t#=D!G!T5=&=5p3c_$ymb07$*^"),
				Arguments.of("OAoa1!"),
				Arguments.of("oOAa1!"),
				Arguments.of("1OAoa!"),
				Arguments.of("!OAoa1"),
				Arguments.of("/Pass,Word|123%456+789?|tilda~apostrophe`quote'")
		);
	}
}
