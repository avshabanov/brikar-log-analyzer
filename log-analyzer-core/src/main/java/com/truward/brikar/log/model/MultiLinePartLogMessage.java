package com.truward.brikar.log.model;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a part of multiline log message.
 *
 * @author Alexander Shabanov
 */
public final class MultiLinePartLogMessage extends LogMessage {
  private final String line;

  public MultiLinePartLogMessage(@Nonnull String line) {
    this.line = Objects.requireNonNull(line, "line");
  }

  @Override
  public boolean isMultiLinePart() {
    return true;
  }

  @Nonnull
  @Override
  public String getLogEntry() {
    return line;
  }

  @Nonnull
  @Override
  public List<String> getLines() {
    return Collections.singletonList(line);
  }

  @Override
  public String toString() {
    return "MultiLinePartLogMessage{" +
        "line='" + line + '\'' +
        '}';
  }
}
