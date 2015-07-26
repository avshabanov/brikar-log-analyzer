package com.truward.brikar.log.model;

/**
 * @author Alexander Shabanov
 */
public final class NullLogMessage extends LogMessage {
  public static final NullLogMessage INSTANCE = new NullLogMessage();

  private NullLogMessage() {}

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public String toString() {
    return "NullLogMessage";
  }
}
