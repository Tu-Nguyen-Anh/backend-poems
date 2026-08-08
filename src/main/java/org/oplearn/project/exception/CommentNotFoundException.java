package org.oplearn.project.exception;

import org.oplearn.project.exception.base.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
  public CommentNotFoundException() {
    super("org.oplearn.project.exception.CommentNotFoundException");
  }
}
