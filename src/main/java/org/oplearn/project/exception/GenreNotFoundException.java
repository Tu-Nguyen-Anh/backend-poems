package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class GenreNotFoundException extends NotFoundException {
  public GenreNotFoundException() {
    super("org.oplearn.project.exception.GenreNotFoundException");
  }
}
