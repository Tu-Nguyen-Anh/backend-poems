package org.oplearn.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "user.username.not_blank")
  private String username;

  @Email(message = "user.email.invalid")
  private String email;


  private String phoneNumber;

  @NotBlank(message = "user.password.not_blank")
  @Size(min = 8, message = "user.password.min_length")
  private String password;
}
