package org.oplearn.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.oplearn.project.entity.base.BaseEntity;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "password")
public class User extends BaseEntity {
  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "email")
  private String email;

  @Column(columnDefinition = "role_enum")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private UserRole role;
}
