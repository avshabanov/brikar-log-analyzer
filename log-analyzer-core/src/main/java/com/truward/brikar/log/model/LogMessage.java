package com.truward.brikar.log.model;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Represents log message.
 *
 * @author Alexander Shabanov
 */
public abstract class LogMessage {

  public boolean isNull() {
    return false;
  }

  public boolean isMultiLinePart() {
    return false;
  }

  @Nonnull
  public String getLogEntry() {
    final List<String> lines = getLines();
    if (lines.isEmpty()) {
      throw new IllegalStateException("Log lines are empty");
    }
    return lines.get(0);
  }

  @Nonnull
  public List<String> getLines() {
    throw new UnsupportedOperationException();
  }

  public void addLine(@Nonnull String value) {
    throw new UnsupportedOperationException();
  }

  public long getUnixTime() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public Severity getSeverity() {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public Map<String, Object> getAttributes() {
    throw new UnsupportedOperationException();
  }
}
