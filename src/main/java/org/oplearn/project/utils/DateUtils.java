package org.oplearn.project.utils;

import java.time.Instant;

public class DateUtils {
  private DateUtils() {
  }

  public static String getCurrentDateString() {
    return Instant.now().toString();
  }
}
