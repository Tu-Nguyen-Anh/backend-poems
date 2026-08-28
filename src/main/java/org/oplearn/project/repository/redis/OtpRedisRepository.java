package org.oplearn.project.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.PendingRegisterData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OtpRedisRepository {
  private static final String PENDING_REGISTER_PREFIX = "auth:pending_register:";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public void savePendingRegistration(String email, PendingRegisterData data, Duration ttl) {
    try {
      String json = objectMapper.writeValueAsString(data);
      redisTemplate.opsForValue().set(PENDING_REGISTER_PREFIX + email, json, ttl);
    } catch (JsonProcessingException e) {
      log.error("Lỗi parse JSON khi lưu đăng ký tạm vào Redis: {}", e.getMessage(), e);
      throw new RuntimeException("Lỗi lưu dữ liệu đăng ký tạm thời");
    }
  }

  public Optional<PendingRegisterData> getPendingRegistration(String email) {
    String json = redisTemplate.opsForValue().get(PENDING_REGISTER_PREFIX + email);
    if (json == null) {
      return Optional.empty();
    }
    try {
      PendingRegisterData data = objectMapper.readValue(json, PendingRegisterData.class);
      return Optional.of(data);
    } catch (JsonProcessingException e) {
      log.error("Lỗi parse JSON khi đọc đăng ký tạm từ Redis: {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public void deletePendingRegistration(String email) {
    redisTemplate.delete(PENDING_REGISTER_PREFIX + email);
  }

  private static final String FORGOT_PASSWORD_PREFIX = "auth:forgot_pwd:";

  public void saveForgotPasswordOtp(String email, String otp, Duration ttl) {
    redisTemplate.opsForValue().set(FORGOT_PASSWORD_PREFIX + email, otp, ttl);
  }

  public Optional<String> getForgotPasswordOtp(String email) {
    String otp = redisTemplate.opsForValue().get(FORGOT_PASSWORD_PREFIX + email);
    return Optional.ofNullable(otp);
  }

  public void deleteForgotPasswordOtp(String email) {
    redisTemplate.delete(FORGOT_PASSWORD_PREFIX + email);
  }
}
