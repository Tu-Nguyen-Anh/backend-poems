package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.User;
import org.oplearn.project.entity.UserRole;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
  Long id;
  Instant createdAt;
  String username;
  String phoneNumber;
  String email;
  UserRole role;
  Instant updatedAt;

  public static UserResponse from(User user) {
    return new UserResponse(
      user.getId(),
      user.getCreatedAt(),
      user.getUsername(),
      user.getPhoneNumber(),
      user.getEmail(),
      user.getRole(),
      user.getUpdatedAt()
    );
  }
}
