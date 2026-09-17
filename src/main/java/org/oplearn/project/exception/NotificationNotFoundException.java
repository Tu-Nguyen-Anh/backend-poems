package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {
  public NotificationNotFoundException() {
    super("org.oplearn.project.exception.NotificationNotFoundException");
  }
}
