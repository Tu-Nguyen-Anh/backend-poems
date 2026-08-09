package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class FeedbackNotFoundException extends NotFoundException {
  public FeedbackNotFoundException() {
    super("org.oplearn.project.exception.feedbackNotFoundException");
  }
}
