package org.oplearn.project.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.ROLES_CLAIM;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TOKEN_TYPE_ACCESS;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TOKEN_TYPE_CLAIM;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TOKEN_TYPE_REFRESH;

@Slf4j
@Component
public class JwtTokenProvider {
  /** Khóa dev công khai trong application.yml — nếu prod dùng khóa này, ai cũng ký được token admin. */
  private static final String DEFAULT_DEV_SECRET =
    "ZGV2LW9ubHktc2VjcmV0LWtleS1jaGFuZ2UtbWUtaW4tcHJvZHVjdGlvbi0xMjM0NTY3OA==";

  private final SecretKey secretKey;
  private final long expirationMs;
  private final long refreshExpirationMs;

  public JwtTokenProvider(
        @Value("${security.jwt.secret}") String base64Secret,
        @Value("${security.jwt.expiration-ms}") long expirationMs,
        @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs
  ) {
    if (DEFAULT_DEV_SECRET.equals(base64Secret)) {
      log.error("!!! BẢO MẬT: đang dùng JWT secret MẶC ĐỊNH công khai. Bất kỳ ai cũng có thể "
        + "giả mạo token ADMIN. Hãy đặt biến môi trường JWT_SECRET (chuỗi base64 ngẫu nhiên >= 256-bit) "
        + "cho môi trường thật NGAY.");
    }
    this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    this.expirationMs = expirationMs;
    this.refreshExpirationMs = refreshExpirationMs;
  }

  public long getExpirationMs() {
    return expirationMs;
  }

  public long getRefreshExpirationMs() {
    return refreshExpirationMs;
  }

  public String generateAccessToken(String subject, List<String> roles) {
    return buildToken(subject, Map.of(ROLES_CLAIM, roles, TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS), expirationMs);
  }

  public String generateRefreshToken(String subject) {
    return buildToken(subject, Map.of(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH), refreshExpirationMs);
  }

  /**
   * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
   */
  public Claims parseClaims(String token) {
    return Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
  }

  public boolean isAccessToken(Claims claims) {
    return TOKEN_TYPE_ACCESS.equals(claims.get(TOKEN_TYPE_CLAIM));
  }

  public boolean isRefreshToken(Claims claims) {
    return TOKEN_TYPE_REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM));
  }

  private String buildToken(String subject, Map<String, Object> claims, long ttlMs) {
    Date now = new Date();
    return Jwts.builder()
          .claims(claims)
          .id(UUID.randomUUID().toString())
          .subject(subject)
          .issuedAt(now)
          .expiration(new Date(now.getTime() + ttlMs))
          .signWith(secretKey)
          .compact();
  }
}
