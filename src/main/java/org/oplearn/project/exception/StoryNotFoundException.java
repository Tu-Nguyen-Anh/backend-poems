package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class StoryNotFoundException extends NotFoundException {
  public StoryNotFoundException() {
    super("org.oplearn.project.exception.StoryNotFoundException");
  }
}
