package org.oplearn.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.GoogleLoginRequest;
import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.request.RefreshTokenRequest;
import org.oplearn.project.dto.request.RegisterRequest;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.dto.response.TokenResponse;
import org.oplearn.project.exception.InvalidRefreshTokenException;
import org.oplearn.project.security.RefreshCookieManager;
import org.oplearn.project.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.Objects;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.AUTHORIZATION;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TYPE_TOKEN;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service;
  private final RefreshCookieManager cookieManager;

  @PostMapping("/login")
  public ResponseEntity<ResponseGeneral<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    log.info("(login) username: {}", request.getUsername());
    return issueWithCookie(service.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ResponseGeneral<TokenResponse>> refresh(
        HttpServletRequest httpRequest,
        @RequestBody(required = false) RefreshTokenRequest request
  ) {
    log.info("(refresh)");
    // Ưu tiên refresh token trong cookie HttpOnly; fallback body (chỉ dùng cho
    // lần migrate đầu của user còn giữ token cũ ở localStorage).
    String refreshToken = cookieManager.read(httpRequest);
    if (!StringUtils.hasText(refreshToken) && request != null) {
      refreshToken = request.getRefreshToken();
    }
    if (!StringUtils.hasText(refreshToken)) {
      throw new InvalidRefreshTokenException();
    }
    return issueWithCookie(service.refresh(refreshToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<ResponseGeneral<Void>> logout(
        HttpServletRequest httpRequest,
        @RequestBody(required = false) RefreshTokenRequest request,
        @RequestHeader(name = AUTHORIZATION, required = false) String authorizationHeader
  ) {
    log.info("(logout)");
    String refreshToken = cookieManager.read(httpRequest);
    if (!StringUtils.hasText(refreshToken) && request != null) {
      refreshToken = request.getRefreshToken();
    }
    service.logout(refreshToken, extractAccessToken(authorizationHeader));
    return cookieManager.withClearedCookie(ResponseGeneral.ofSuccess(SUCCESS_MESSAGE));
  }

  @PostMapping("/register")
  public ResponseEntity<ResponseGeneral<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
    log.info("(register) username: {}", request.getUsername());
    return issueWithCookie(service.register(request));
  }

  @PostMapping("/login/google")
  public ResponseEntity<ResponseGeneral<TokenResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
    log.info("(loginWithGoogle)");
    return issueWithCookie(service.loginWithGoogle(request));
  }

  /**
   * Đặt refresh token vào cookie HttpOnly và XOÁ khỏi JSON body (JS không đọc
   * được → chống XSS). Body chỉ còn access token.
   */
  private ResponseEntity<ResponseGeneral<TokenResponse>> issueWithCookie(TokenResponse tokens) {
    String refreshToken = tokens.getRefreshToken();
    tokens.setRefreshToken(null);
    return cookieManager.withRefreshCookie(ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, tokens), refreshToken);
  }

  private String extractAccessToken(String authorizationHeader) {
    if (Objects.isNull(authorizationHeader) || !authorizationHeader.startsWith(TYPE_TOKEN)) {
      return null;
    }
    return authorizationHeader.substring(TYPE_TOKEN.length());
  }
}
