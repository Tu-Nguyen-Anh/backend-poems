package org.oplearn.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.oplearn.project.entity.base.BaseEntity;
import org.oplearn.project.enums.AuthProvider;
import org.oplearn.project.enums.UserRole;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "password")
public class User extends BaseEntity {
  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "email")
  private String email;

  @Column(columnDefinition = "role_enum")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private UserRole role;

  @Column(columnDefinition = "auth_provider_enum")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Builder.Default
  private AuthProvider provider = AuthProvider.LOCAL;

  @Column(name = "provider_id")
  private String providerId;
}
