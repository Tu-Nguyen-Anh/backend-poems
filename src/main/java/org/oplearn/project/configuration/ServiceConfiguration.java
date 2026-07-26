package org.oplearn.project.configuration;

import org.oplearn.project.service.base.MessageService;
import org.oplearn.project.service.impl.MessageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {
  @Bean
  @ConditionalOnProperty(
        value = "conditional.bean.messageService",
        havingValue = "true",
        matchIfMissing = true
  )
  public MessageService messageService(MessageSource messageSource) {
    return new MessageServiceImpl(messageSource);
  }

}
