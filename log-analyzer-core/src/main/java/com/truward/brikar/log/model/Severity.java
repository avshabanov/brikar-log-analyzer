package com.truward.brikar.log.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents SLF4J log severities.
 *
 * @author Alexander Shabanov
 */
public enum Severity {
  TRACE,
  DEBUG,
  ERROR,
  INFO,
  WARN;

  private static final Map<String, Severity> SEVERITY_MAP;
  static {
    final Severity[] values = Severity.values();
    final Map<String, Severity> map = new HashMap<>(values.length * 2);
    for (final Severity severity : values) {
      map.put(severity.name(), severity);
    }
    SEVERITY_MAP = Collections.unmodifiableMap(map);
  }

  public static Severity fromString(String value, Severity defaultSeverity) {
    final Severity result = SEVERITY_MAP.get(value);
    return result != null ? result : defaultSeverity;
  }
}
