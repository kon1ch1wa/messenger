package ru.shutoff.messenger.domain.user_mgmt.validation;

import java.util.Objects;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberConstraint, String> {
	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		try {
			if (s == null) {
				return true;
			}
			s = normalizeNumber(s);
			if (s.equals("CANNOT.BE.NORMALIZED")) {
				return false;
			}
			PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(s, "RU");
			return phoneNumberUtil.isValidNumber(phoneNumber);
		} catch (Exception ex) {
			return false;
		}
	}

	private String normalizeNumber(String phoneNumber) {
		phoneNumber = phoneNumber.trim();
		if (!Objects.equals(phoneNumber.charAt(0), '+') && !Objects.equals(phoneNumber.charAt(0), '8')) {
			return "CANNOT.BE.NORMALIZED";
		}
		StringBuilder sb = new StringBuilder(phoneNumber.charAt(0));
		for (int i = 1; i < phoneNumber.length(); i++) {
			if (
				!Objects.equals(phoneNumber.charAt(i), '(') &&
				!Objects.equals(phoneNumber.charAt(i), ')') &&
				!Objects.equals(phoneNumber.charAt(i), '-') &&
				!Objects.equals(phoneNumber.charAt(i), ' ')
			) {
				if (Character.isDigit(phoneNumber.charAt(i))) {
					sb.append(phoneNumber.charAt(i));
				}
				else {
					return "CANNOT.BE.NORMALIZED";
				}
			}
		}
		return new String(sb);
	}
}
