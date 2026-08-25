package org.oplearn.project.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.ForgotPasswordRequest;
import org.oplearn.project.dto.request.GoogleLoginRequest;
import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.request.PendingRegisterData;
import org.oplearn.project.dto.request.RegisterRequest;
import org.oplearn.project.dto.request.ResetPasswordRequest;
import org.oplearn.project.dto.request.VerifyOtpRequest;
import org.oplearn.project.dto.response.TokenResponse;
import org.oplearn.project.enums.AuthProvider;
import org.oplearn.project.entity.User;
import org.oplearn.project.enums.UserRole;
import org.oplearn.project.exception.EmailAlreadyExistedException;
import org.oplearn.project.exception.InvalidCredentialException;
import org.oplearn.project.exception.InvalidOtpException;
import org.oplearn.project.exception.InvalidRefreshTokenException;
import org.oplearn.project.exception.OtpExpiredException;
import org.oplearn.project.exception.UserNotFoundException;
import org.oplearn.project.exception.UsernameAlreadyExistedException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.repository.redis.OtpRedisRepository;
import org.oplearn.project.repository.redis.TokenRedisRepository;
import org.oplearn.project.security.jwt.JwtTokenProvider;
import org.oplearn.project.service.AuthService;
import org.oplearn.project.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.TYPE_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  @Value("${google.client-id:}")
  private String googleClientId;

  private final UserRepository userRepository;
  private final TokenRedisRepository tokenRedisRepository;
  private final OtpRedisRepository otpRedisRepository;
  private final EmailService emailService;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;

  @Override
  public TokenResponse login(LoginRequest request) {
    User user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
          .orElseThrow(InvalidCredentialException::new);

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new InvalidCredentialException();
    }
    return issueTokens(user);
  }

  @Override
  public void register(RegisterRequest request) {
    if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }
    if (StringUtils.hasText(request.getEmail())
      && userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
      throw new EmailAlreadyExistedException();
    }

    String otpCode = String.valueOf((int) ((Math.random() * 900000) + 100000));

    PendingRegisterData pendingData = PendingRegisterData.builder()
      .username(request.getUsername())
      .email(request.getEmail())
      .phoneNumber(request.getPhoneNumber())
      .encodedPassword(passwordEncoder.encode(request.getPassword()))
      .otpCode(otpCode)
      .build();

    otpRedisRepository.savePendingRegistration(request.getEmail(), pendingData, Duration.ofMinutes(5));

    emailService.sendHtmlEmail(
      request.getEmail(),
      "Xác thực tài khoản - Backend Poems",
      "mail/welcome-email",
      Map.of("recipientName", request.getUsername(), "otpCode", otpCode)
    );
  }

  @Override
  public TokenResponse verifyOtp(VerifyOtpRequest request) {
    PendingRegisterData pendingData = otpRedisRepository.getPendingRegistration(request.getEmail())
          .orElseThrow(OtpExpiredException::new);

    if (!pendingData.getOtpCode().equals(request.getOtp())) {
      throw new InvalidOtpException();
    }

    if (userRepository.existsByUsernameAndIsDeletedFalse(pendingData.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }
    if (StringUtils.hasText(pendingData.getEmail())
      && userRepository.existsByEmailAndIsDeletedFalse(pendingData.getEmail())) {
      throw new EmailAlreadyExistedException();
    }

    // Chính thức lưu User vào Database
    User user = User.builder()
      .username(pendingData.getUsername())
      .email(pendingData.getEmail())
      .phoneNumber(pendingData.getPhoneNumber())
      .password(pendingData.getEncodedPassword())
      .role(UserRole.USER)
      .build();

    userRepository.save(user);

    otpRedisRepository.deletePendingRegistration(request.getEmail());

    return issueTokens(user);
  }

  @Override
  public void forgotPassword(ForgotPasswordRequest request) {
    User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
          .orElseThrow(UserNotFoundException::new);

    String otpCode = String.valueOf((int) ((Math.random() * 900000) + 100000));
    otpRedisRepository.saveForgotPasswordOtp(request.getEmail(), otpCode, Duration.ofMinutes(5));

    emailService.sendHtmlEmail(
          request.getEmail(),
          "Yêu cầu đặt lại mật khẩu - Backend Poems 📜",
          "mail/forgot-password-email",
          Map.of("recipientName", user.getUsername(), "otpCode", otpCode)
    );
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    String savedOtp = otpRedisRepository.getForgotPasswordOtp(request.getEmail())
          .orElseThrow(OtpExpiredException::new);

    if (!savedOtp.equals(request.getOtp())) {
      throw new InvalidOtpException();
    }

    User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
          .orElseThrow(UserNotFoundException::new);

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    otpRedisRepository.deleteForgotPasswordOtp(request.getEmail());
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    Claims claims = parseRefreshTokenClaims(refreshToken);

    Long userId = tokenRedisRepository.findUserIdByRefreshToken(claims.getId())
          .orElseThrow(InvalidRefreshTokenException::new);

    User user = userRepository.findByIdAndIsDeletedFalse(userId)
          .orElseThrow(InvalidRefreshTokenException::new);

    tokenRedisRepository.deleteRefreshToken(claims.getId());
    return issueTokens(user);
  }

  @Override
  public void logout(String refreshToken, String accessToken) {
    try {
      Claims claims = parseRefreshTokenClaims(refreshToken);
      tokenRedisRepository.deleteRefreshToken(claims.getId());
    } catch (InvalidRefreshTokenException ex) {
      log.warn("(logout) skip invalid refresh token");
    }

    if (StringUtils.hasText(accessToken)) {
      blacklistAccessToken(accessToken);
    }
  }

  @Override
  public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
    GoogleIdToken.Payload payload = verifyGoogleToken(request.getToken());
    String email = payload.getEmail();
    String googleUserId = payload.getSubject();

    User user = userRepository.findByProviderAndProviderIdAndIsDeletedFalse(AuthProvider.GOOGLE, googleUserId)
      .or(() -> userRepository.findByEmailAndIsDeletedFalse(email))
      .orElseGet(() -> {
        // 3. Nếu chưa có -> Tự động đăng ký User mới
        String username = generateUniqueUsername(email, googleUserId);
        User newUser = User.builder()
          .username(username)
          .email(email)
          .role(UserRole.USER)
          .provider(AuthProvider.GOOGLE)
          .providerId(googleUserId)
          .build();
        return userRepository.save(newUser);
  });
    return issueTokens(user);
  }

  private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
    try {
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(List.of(googleClientId))
        .build();

      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken == null) {
        throw new InvalidCredentialException();
      }
      return idToken.getPayload();
    } catch (Exception e) {
      log.error("(verifyGoogleToken) error: {}", e.getMessage());
      throw new InvalidCredentialException();
    }
  }

  private String generateUniqueUsername(String email, String googleUserId) {
    String baseUsername = (email != null && email.contains("@"))
      ? email.substring(0, email.indexOf("@")).replaceAll("[^a-zA-Z0-9_.]", "")
      : "google_user";

    if (baseUsername.length() > 40) {
      baseUsername = baseUsername.substring(0, 40);
    }

    String username = baseUsername;
    int suffix = 1;
    while (userRepository.existsByUsernameAndIsDeletedFalse(username)) {
      username = baseUsername + "_" + suffix++;
    }
    return username;
  }

  private Claims parseRefreshTokenClaims(String refreshToken) {
    try {
      Claims claims = jwtTokenProvider.parseClaims(refreshToken);
      if (!jwtTokenProvider.isRefreshToken(claims) || !StringUtils.hasText(claims.getId())) {
        throw new InvalidRefreshTokenException();
      }
      return claims;
    } catch (JwtException | IllegalArgumentException ex) {
      log.warn("(parseRefreshTokenClaims) invalid refresh token: {}", ex.getMessage());
      throw new InvalidRefreshTokenException();
    }
  }

  private void blacklistAccessToken(String accessToken) {
    try {
      Claims claims = jwtTokenProvider.parseClaims(accessToken);
      Duration remainingTtl = Duration.between(Instant.now(), claims.getExpiration().toInstant());
      tokenRedisRepository.blacklistAccessToken(claims.getId(), remainingTtl);
    } catch (JwtException | IllegalArgumentException ex) {
      log.warn("(blacklistAccessToken) skip invalid access token: {}", ex.getMessage());
    }
  }

  private TokenResponse issueTokens(User user) {
    String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId(), List.of(user.getRole().name()));
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

    String refreshTokenId = jwtTokenProvider.parseClaims(refreshToken).getId();
    tokenRedisRepository.saveRefreshToken(
          refreshTokenId,
          user.getId(),
          Duration.ofMillis(jwtTokenProvider.getRefreshExpirationMs())
    );

    return TokenResponse.of(
          accessToken,
          refreshToken,
          TYPE_TOKEN.trim(),
          jwtTokenProvider.getExpirationMs() / 1000
    );
  }
}
