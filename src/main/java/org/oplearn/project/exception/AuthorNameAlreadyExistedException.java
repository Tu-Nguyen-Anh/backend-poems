package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class AuthorNameAlreadyExistedException extends ConflictException {
  public AuthorNameAlreadyExistedException() {
    super("org.oplearn.project.exception.AuthorNameAlreadyExistedException");
  }
}
