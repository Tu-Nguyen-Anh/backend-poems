package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class PoemCompositionNotFoundException extends NotFoundException {
  public PoemCompositionNotFoundException() {
    super("org.oplearn.project.exception.PoemCompositionNotFoundException");
  }
}
