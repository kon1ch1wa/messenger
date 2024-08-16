package ru.shutoff.messenger.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.shutoff.messenger.model.User;
import ru.shutoff.messenger.repository.UserInfoRepo;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserInfoRepo userInfoRepo;
	private final PasswordEncoder passwordEncoder;
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
