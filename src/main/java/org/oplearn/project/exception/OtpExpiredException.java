package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class OtpExpiredException extends BadRequestException {
  public OtpExpiredException() {
    super("org.oplearn.project.exception.OtpExpiredException");
  }
}
