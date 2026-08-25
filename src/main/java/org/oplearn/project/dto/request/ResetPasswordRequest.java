package org.oplearn.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResetPasswordRequest {

  @NotBlank(message = "auth.email.not_blank")
  @Email(message = "user.email.invalid")
  private String email;

  @NotBlank(message = "auth.otp.not_blank")
  private String otp;

  @NotBlank(message = "user.new_password.not_blank")
  @Size(min = 8, message = "user.password.min_length")
  private String newPassword;
}
