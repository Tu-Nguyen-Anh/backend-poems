package org.oplearn.project.exception;

import org.oplearn.project.exception.base.UnauthorizedException;

public class UserUnauthorizedException extends UnauthorizedException {
  public UserUnauthorizedException() {
    super("org.oplearn.project.exception.base.UnauthorizedException");
  }
}
