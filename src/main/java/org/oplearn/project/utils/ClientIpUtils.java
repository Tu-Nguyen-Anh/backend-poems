package org.oplearn.project.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public final class ClientIpUtils {

  private ClientIpUtils() {}

  /**
   * Trích xuất định danh người dùng:
   * - Nếu đã đăng nhập -> trả về "user:{username}"
   * - Nếu là khách vãng lai -> trả về "ip:{clientIp}"
   */
  public static String getUserIdentifier(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
      return "user:" + auth.getName();
    }
    return "ip:" + getClientIp(request);
  }

  /**
   * Trích xuất IP thực của Client (hỗ trợ cả trường hợp đi qua Nginx / Cloudflare Proxy).
   */
  public static String getClientIp(HttpServletRequest request) {
    if (request == null) {
      return "unknown";
    }

    String ip = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
      return ip.split(",")[0].trim();
    }

    ip = request.getHeader("X-Real-IP");
    if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
      return ip.trim();
    }

    return request.getRemoteAddr();
  }
}
