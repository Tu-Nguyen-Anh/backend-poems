package org.oplearn.project.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordEvent {
  private String to;
  private String subject;
  private String templateName;
  private Map<String, Object> variables;
  private Instant createdAt;
}
