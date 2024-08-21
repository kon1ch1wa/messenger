package ru.shutoff.messenger.domain.user_mgmt.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.shutoff.messenger.domain.user_mgmt.model.User;
import ru.shutoff.messenger.domain.user_mgmt.repository.UserInfoRepo;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserInfoRepo userInfoRepo;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userInfoRepo.getByLogin(username);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("User %s not found", username));
		}
		return new org.springframework.security.core.userdetails.User(
			user.getLogin(), user.getPassword(), new ArrayList<>()
		);
	}
}
