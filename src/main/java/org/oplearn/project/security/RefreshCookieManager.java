package org.oplearn.project.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Quản lý cookie chứa refresh token (HttpOnly). Refresh token KHÔNG còn nằm
 * trong JSON body (JS đọc được) — chỉ đi qua cookie này, giảm rủi ro XSS đánh
 * cắp token sống dài và triệt tiêu race đa-tab (FE không tự đọc/ghi RT nữa).
 *
 * <p>Thuộc tính cookie:
 * <ul>
 *   <li>{@code HttpOnly} — JS không đọc được.</li>
 *   <li>{@code Path=/api/v1/auth} — chỉ gửi kèm request tới nhóm endpoint auth
 *       (refresh/logout), không phình mọi API call.</li>
 *   <li>{@code Secure}/{@code SameSite} lấy từ config (PROD: secure=true).</li>
 *   <li>{@code Max-Age} = TTL refresh token (khớp Redis store).</li>
 * </ul>
 */
@Component
@Slf4j
public class RefreshCookieManager {

  public static final String COOKIE_NAME = "refresh_token";
  private static final String COOKIE_PATH = "/api/v1/auth";

  @Value("${app.auth.cookie.secure:false}")
  private boolean secure;

  @Value("${app.auth.cookie.same-site:Lax}")
  private String sameSite;

  @Value("${security.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  /**
   * Cảnh báo cấu hình nguy hiểm lúc boot: refresh token sống dài mà cookie KHÔNG
   * Secure = lộ qua HTTP thuần (MITM chiếm session). PROD phải set
   * {@code AUTH_COOKIE_SECURE=true}. {@code SameSite=None} bắt buộc Secure.
   */
  @PostConstruct
  void warnInsecureCookie() {
    if (!secure) {
      log.warn("(auth.cookie) SECURE=false — refresh_token đi qua HTTP thuần. "
            + "Chỉ chấp nhận ở local dev; PROD BẮT BUỘC set AUTH_COOKIE_SECURE=true.");
    }
    if ("None".equalsIgnoreCase(sameSite) && !secure) {
      log.error("(auth.cookie) SameSite=None mà Secure=false — trình duyệt sẽ TỪ CHỐI "
            + "cookie refresh, đăng nhập hỏng. Set AUTH_COOKIE_SECURE=true.");
    }
  }

  /** Đọc refresh token từ cookie request; null nếu không có. */
  public String read(HttpServletRequest request) {
    if (request == null || request.getCookies() == null) {
      return null;
    }
    for (Cookie c : request.getCookies()) {
      if (COOKIE_NAME.equals(c.getName())) {
        String v = c.getValue();
        return (v == null || v.isBlank()) ? null : v;
      }
    }
    return null;
  }

  /** Header Set-Cookie ghi refresh token với TTL = TTL của token. */
  public String buildSetCookie(String refreshToken) {
    return ResponseCookie.from(COOKIE_NAME, refreshToken)
          .httpOnly(true)
          .secure(secure)
          .path(COOKIE_PATH)
          .maxAge(refreshExpirationMs / 1000)
          .sameSite(sameSite)
          .build()
          .toString();
  }

  /** Header Set-Cookie xoá cookie (Max-Age=0). */
  public String buildClearCookie() {
    return ResponseCookie.from(COOKIE_NAME, "")
          .httpOnly(true)
          .secure(secure)
          .path(COOKIE_PATH)
          .maxAge(0)
          .sameSite(sameSite)
          .build()
          .toString();
  }

  /** Bọc body vào ResponseEntity kèm Set-Cookie refresh token. */
  public <T> ResponseEntity<T> withRefreshCookie(T body, String refreshToken) {
    return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, buildSetCookie(refreshToken))
          .body(body);
  }

  /** Bọc body vào ResponseEntity kèm Set-Cookie xoá refresh token. */
  public <T> ResponseEntity<T> withClearedCookie(T body) {
    return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, buildClearCookie())
          .body(body);
  }
}
