package ru.shutoff.messenger.chat_logic.websocket;

import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import ru.shutoff.messenger.exception.NotAuthorizedException;
import ru.shutoff.messenger.security.JwtUtils;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
	private final JwtUtils jwtUtils;
	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
		List<String> cookies = request.getHeaders().get("Cookie");
		if (cookies == null) {
			throw new NotAuthorizedException("Not authorized to system");
		}
		String token = cookies.stream().filter(x -> x.equals("JwtToken")).findAny().orElseThrow(() -> new NotAuthorizedException("Not Authorized to chat"));
		String username = jwtUtils.getUsernameFromJwtToken(token);
		return new UserPrincipal(username);
	}
}
