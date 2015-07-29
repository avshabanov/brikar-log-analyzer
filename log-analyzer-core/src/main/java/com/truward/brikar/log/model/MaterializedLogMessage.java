package com.truward.brikar.log.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents parsed log message.
 *
 * @author Alexander Shabanov
 */
public final class MaterializedLogMessage extends LogMessage {
  private final long unixTime;
  private final Severity severity;
  private List<String> lines = new ArrayList<>();
  private Map<String, Object> attributes = new HashMap<>();

  public MaterializedLogMessage(long unixTime, @Nonnull Severity severity, @Nonnull String logEntry) {
    this.unixTime = unixTime;
    this.severity = Objects.requireNonNull(severity, "severity");
    this.lines.add(Objects.requireNonNull(logEntry, "logEntry"));
  }

  @Override
  public long getUnixTime() {
    return unixTime;
  }

  @Nonnull
  @Override
  public Severity getSeverity() {
    return severity;
  }

  @Nonnull
  @Override
  public List<String> getLines() {
    return Collections.unmodifiableList(lines);
  }

  @Nonnull
  @Override
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  @Override
  public void addLine(@Nonnull String value) {
    this.lines.add(value);
  }

  public void putAttribute(@Nonnull String key, @Nullable Object value) {
    this.attributes.put(key, value);
  }

  @Override
  public String toString() {
    return "MaterializedLogMessage{" +
            "unixTime=" + getUnixTime() +
            ", severity=" + getSeverity() +
            ", attributes=" + getAttributes() +
            ", logEntry='" + getLogEntry() + '\'' +
            ", stacktrace=" + getLines() +
            '}';
  }
}
