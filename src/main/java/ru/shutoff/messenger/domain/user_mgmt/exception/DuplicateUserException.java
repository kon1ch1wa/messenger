package ru.shutoff.messenger.domain.user_mgmt.exception;

import org.springframework.dao.DuplicateKeyException;

public class DuplicateUserException extends DuplicateKeyException {
	public DuplicateUserException(String message) {
		super(message);
	}
}
