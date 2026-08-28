package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class InvalidOtpException extends BadRequestException {
  public InvalidOtpException() {
    super("org.oplearn.project.exception.InvalidOtpException");
  }
}
