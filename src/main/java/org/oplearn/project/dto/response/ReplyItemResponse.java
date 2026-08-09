package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReplyItemResponse {
  private Long id;
  private Long userId;
  private String content;
  private String username;
  private Instant createdAt;
}
