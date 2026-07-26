package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.oplearn.project.service.base.MessageService;
import org.springframework.context.MessageSource;

import java.util.Locale;

@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
  private final MessageSource messageSource;

  public String getMessage(String code, String language) {
    try {
      return messageSource.getMessage(code, null, new Locale(language));
    } catch (Exception ex) {
      return code;
    }
  }
}

