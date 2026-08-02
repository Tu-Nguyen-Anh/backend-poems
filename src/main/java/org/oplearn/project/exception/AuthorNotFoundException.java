package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class AuthorNotFoundException extends NotFoundException {
  public AuthorNotFoundException() {
    super("org.oplearn.project.exception.AuthorNotFoundException");
  }
}
