package com.truward.brikar.log.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parses strings like <code>a=1, b=22, cc=3</code>.
 */
public final class CommaSeparatedValueParser {
  private final String value;
  private int index = 0;
  private State state = State.READ_KEY;

  public CommaSeparatedValueParser(@Nonnull String value) {
    this.value = Objects.requireNonNull(value, "value");
  }

  @Nullable
  public String nextKey() {
    if (state != State.READ_KEY) {
      throw new IllegalStateException("state=" + state);
    }
    final int nextIndex = value.indexOf('=', index);
    if (nextIndex < 0) {
      return null;
    }

    final String val = value.substring(index, nextIndex);
    index = nextIndex + 1;
    state = State.READ_VALUE;
    return val;
  }

  @Nullable
  public String nextValue() {
    if (state != State.READ_VALUE) {
      throw new IllegalStateException("state=" + state);
    }
    int inc = 1;
    int nextIndex = value.length();
    for (int i = index; i < value.length(); ++i) {
      char ch = value.charAt(i);
      if (ch == ' ') {
        nextIndex = i;
        break;
      }

      if (ch == ',') {
        nextIndex = i;
        inc = 2;
        break;
      }

    }

    final String val = value.substring(index, nextIndex);
    index = nextIndex + inc;
    state = State.READ_KEY;
    return val;
  }

  public void readToMap(@Nonnull Map<String, String> sink) {
    for (;;) {
      final String key = nextKey();
      if (key == null) {
        return;
      }

      final String value = nextValue();
      sink.put(key, value);
    }
  }

  @Nonnull
  public Map<String, String> readAsMap() {
    final Map<String, String> result = new HashMap<>();
    readToMap(result);
    return result;
  }

  private enum State {
    READ_KEY,
    READ_VALUE
  }
}
