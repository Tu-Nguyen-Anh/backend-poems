package org.oplearn.project.websocket;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.repository.redis.TokenRedisRepository;
import org.oplearn.project.security.jwt.JwtTokenProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final TokenRedisRepository tokenRedisRepository;

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes
  ) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      String token = servletRequest.getServletRequest().getParameter("token");
      if (StringUtils.hasText(token)) {
        try {
          if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
          }
          Claims claims = jwtTokenProvider.parseClaims(token);

          if (jwtTokenProvider.isAccessToken(claims)
              && !tokenRedisRepository.isAccessTokenBlacklisted(claims.getId())) {
            Object rawUserId = claims.get("userId");
            if (rawUserId == null) {
              rawUserId = claims.get("id");
            }
            if (rawUserId instanceof Number number) {
              Long userId = number.longValue();
              attributes.put("userId", userId);
              attributes.put("username", claims.getSubject());
              log.info("🔑 [WebSocket Handshake] Xác thực thành công userId = {}, username = {}", userId, claims.getSubject());
            }
          } else {
            log.warn("⚠️ [WebSocket Handshake] Token không phải access token hoặc đã bị blacklist");
          }
        } catch (Exception e) {
          log.warn("⚠️ [WebSocket Handshake] Lỗi giải mã token: {}", e.getMessage());
        }
      }
    }
    // Luôn trả về true để cho phép kết nối (dù là guest hay authenticated user)
    return true;
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception
  ) {
  }
}
