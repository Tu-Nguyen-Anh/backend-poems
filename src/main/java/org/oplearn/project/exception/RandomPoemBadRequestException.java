package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class RandomPoemBadRequestException extends BadRequestException {
  public RandomPoemBadRequestException() {
    super("org.oplearn.project.exception.RandomPoemBadRequestException");
  }
}
