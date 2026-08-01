package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class GenreNameAlreadyExistedException extends ConflictException {
  public GenreNameAlreadyExistedException() {
    super("org.oplearn.project.exception.GenreNameAlreadyExistedException");
  }
}
