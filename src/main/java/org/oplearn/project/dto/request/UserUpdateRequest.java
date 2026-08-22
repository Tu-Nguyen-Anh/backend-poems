package org.oplearn.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oplearn.project.enums.UserRole;

/**
 * Payload cập nhật user (admin sửa thông tin / nâng vai trò). Khác {@link UserRequest}
 * ở chỗ password là TUỲ CHỌN — chỉ đổi mật khẩu khi có nhập, để sửa thông tin
 * không vô tình reset mật khẩu.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "password")
public class UserUpdateRequest {
  @NotBlank(message = "user.username.not_blank")
  private String username;

  private String phoneNumber;

  @Email(message = "user.email.invalid")
  private String email;

  private UserRole role;

  /** Bỏ trống = giữ nguyên mật khẩu; có nhập thì tối thiểu 8 ký tự. */
  @Size(min = 8, message = "user.password.min_length")
  private String password;
}
