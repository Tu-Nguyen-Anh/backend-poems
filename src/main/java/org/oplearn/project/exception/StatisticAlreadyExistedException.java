package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class StatisticAlreadyExistedException extends ConflictException {
  public StatisticAlreadyExistedException() {
    super("org.oplearn.project.exception.StatisticAlreadyExistedException");
  }
}
