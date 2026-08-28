package org.oplearn.project.service;

import org.oplearn.project.dto.request.ForgotPasswordRequest;
import org.oplearn.project.dto.request.GoogleLoginRequest;
import org.oplearn.project.dto.request.LoginRequest;
import org.oplearn.project.dto.request.RegisterRequest;
import org.oplearn.project.dto.request.ResetPasswordRequest;
import org.oplearn.project.dto.request.VerifyOtpRequest;
import org.oplearn.project.dto.response.TokenResponse;

public interface AuthService {
  TokenResponse login(LoginRequest request);

  void register(RegisterRequest request);

  TokenResponse refresh(String refreshToken);

  void logout(String refreshToken, String accessToken);

  TokenResponse loginWithGoogle(GoogleLoginRequest request);

  TokenResponse verifyOtp(VerifyOtpRequest request);

  void forgotPassword(ForgotPasswordRequest request);

  void resetPassword(ResetPasswordRequest request);
}
