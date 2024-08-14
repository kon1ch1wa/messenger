package ru.shutoff.messenger.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shutoff.messenger.exception.NotAuthorizedException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {
	@Value("${jwt.expiration_time}")
	private int JwtExpiration;

	@Value("${jwt.secret}")
	private String JwtSecretKey;

	public static final String JwtCookieName = "JwtToken";

	public String generateJwtToken(Authentication authentication, String username, String password) {
		UserDetails userDetails = new User(username, password, authentication.getAuthorities());
		return Jwts
				.builder()
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date())
				.setExpiration(new Date(new Date().getTime() + JwtExpiration))
				.signWith(key())
				.compact();
	}

	public void refreshJwtToken(Cookie jwtCookie) {
		String jwtToken = jwtCookie.getValue();
		log.debug ("jwtToken: {}", jwtToken);
		Claims claims = Jwts
				.parserBuilder()
				.setSigningKey(key())
				.build()
				.parseClaimsJws(jwtToken)
				.getBody()
				.setIssuedAt(new Date())
				.setExpiration(new Date(new Date().getTime() + JwtExpiration));
		jwtCookie.setValue(Jwts.builder().setClaims(claims).signWith(key()).compact());
	}

	public void invalidateJwtToken(Cookie jwtCookie) {
		jwtCookie.setPath("/invalid");
		jwtCookie.setValue("");
		jwtCookie.setMaxAge(0);
	}

	public Key key() {
		return Keys.hmacShaKeyFor(Encoders.BASE64.encode(JwtSecretKey.getBytes()).getBytes());
	}

	public String getUsernameFromJwtToken(String jwtToken) {
		return Jwts
				.parserBuilder()
				.setSigningKey(key())
				.build()
				.parseClaimsJws(jwtToken)
				.getBody()
				.getSubject();
	}

	public boolean isValidJwtToken(String jwtToken) {
		String msg;
		try {
			Jwts.parserBuilder().setSigningKey(key()).build().parse(jwtToken);
			return true;
		} catch (MalformedJwtException ex) {
			msg = "Invalid JWT Token";
		} catch (UnsupportedJwtException ex) {
			msg = "JWT Token is not supported";
		} catch (ExpiredJwtException ex) {
			msg = "JWT Token expired";
		} catch (IllegalArgumentException ex) {
			msg = "Illegal jwt claim";
		}
		throw new NotAuthorizedException(msg);
	}

	public Cookie formCookie(String jwt) {
		Cookie jwtCookie = new Cookie(JwtCookieName, jwt);
		jwtCookie.setHttpOnly(true);
		jwtCookie.setSecure(true);
		jwtCookie.setPath("/");
		jwtCookie.setMaxAge(JwtExpiration);
		log.debug("Value: {}", jwtCookie.getValue());
		return jwtCookie;
	}
}
