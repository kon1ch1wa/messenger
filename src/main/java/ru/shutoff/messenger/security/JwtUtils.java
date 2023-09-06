package ru.shutoff.messenger.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.shutoff.messenger.exception.NotAuthorizedException;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtils {
	@Value("${jwt.expiration_time}")
	private int JwtExpiration;

	@Value("${jwt.secret}")
	private String JwtSecretKey;

	public String generateJwtToken(Authentication authentication, String username, String password) {
		UserDetails userDetails = new User(username, password, authentication.getAuthorities());
		return Jwts.builder()
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date())
				.setExpiration(new Date(new Date().getTime() + JwtExpiration))
				.signWith(key())
				.compact();
	}

	public void refreshJwtToken(String jwtToken, Cookie jwtCookie) {
		Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(jwtToken).getBody()
				.setIssuedAt(new Date())
				.setExpiration(new Date(new Date().getTime() + JwtExpiration));
		jwtCookie.setValue(Jwts.builder().setClaims(claims).signWith(key()).compact());
	}

	public void invalidateJwtToken(Cookie jwtCookie) {
		jwtCookie.setPath("/");
		jwtCookie.setValue("");
		jwtCookie.setMaxAge(0);
	}

	public Key key() {
		return Keys.hmacShaKeyFor(Encoders.BASE64.encode(JwtSecretKey.getBytes()).getBytes());
		//return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(JwtSecretKey));
		//return Keys.hmacShaKeyFor(JwtSecretKey.getBytes());
	}

	public String getUsernameFromJwtToken(String jwtToken) {
		return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(jwtToken)
				.getBody().getSubject();
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

	public Cookie getJwtCookieFromRequest(HttpServletRequest request) {
		Cookie jwtCookie = null;
		Cookie[] cookieAuth = request.getCookies();
		if (cookieAuth != null) {
			for (Cookie cookie: cookieAuth) {
				if (cookie.getName().equals("JwtToken")) {
					jwtCookie = cookie;
					break;
				}
			}
		}
		return jwtCookie;
	}
}