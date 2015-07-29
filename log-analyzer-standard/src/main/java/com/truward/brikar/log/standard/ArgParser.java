package com.truward.brikar.log.standard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Parser for command line arguments.
 *
 * @author Alexander Shabanov
 */
public final class ArgParser {

  public static final long DEFAULT_SCAN_STREAM_DELAY_MILLIS = 100L;
  public static final long DEFAULT_STOP_FILE_POLLING_DELAY_MILLIS = 1000L;
  public static final int DEFAULT_MAX_STACKTRACE_SIZE = 10000;
  public static final long DEFAULT_MAX_STACKTRACE_POPULATION_TIME_MILLIS = 200L;
  public static final String DEFAULT_ENDPOINT = "stream:file?fileName=/dev/stdout";

  /**
   * Argument parsing result.
   */
  public static final class Result {
    private final long scanStreamDelay;
    private final String stopFileName;
    private final String sourceFileName;
    private final long stopFilePollingDelayMillis;
    private final int maxStacktraceSize;
    private final long maxStacktracePopulationTimeMillis;
    private final String endpoint;

    public Result(long scanStreamDelay,
                  String stopFileName,
                  String sourceFileName,
                  long stopFilePollingDelayMillis,
                  int maxStacktraceSize,
                  long maxStacktracePopulationTimeMillis,
                  String endpoint) {
      if (sourceFileName == null) {
        throw new IllegalArgumentException("Source file name is missing");
      }

      if (scanStreamDelay <= 0) {
        throw new IllegalArgumentException("Scan delay should be a positive number");
      }

      if (stopFilePollingDelayMillis <= 0) {
        throw new IllegalArgumentException("Stop file polling delay should be a positive number");
      }

      if (maxStacktraceSize <= 0) {
        throw new IllegalArgumentException("Max stacktrace size should be a positive number");
      }

      if (maxStacktracePopulationTimeMillis <= 0) {
        throw new IllegalArgumentException("Max stacktrace population time should be a positive number");
      }

      this.scanStreamDelay = scanStreamDelay;
      this.stopFileName = stopFileName;
      this.sourceFileName = sourceFileName;
      this.stopFilePollingDelayMillis = stopFilePollingDelayMillis;
      this.maxStacktraceSize = maxStacktraceSize;
      this.maxStacktracePopulationTimeMillis = maxStacktracePopulationTimeMillis;
      this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
    }

    public long getScanStreamDelay() {
      return scanStreamDelay;
    }

    @Nullable
    public String getStopFileName() {
      return stopFileName;
    }

    @Nonnull
    public String getSourceFileName() {
      return sourceFileName;
    }

    public long getStopFilePollingDelayMillis() {
      return stopFilePollingDelayMillis;
    }

    public int getMaxStacktraceSize() {
      return maxStacktraceSize;
    }

    public long getMaxStacktracePopulationTimeMillis() {
      return maxStacktracePopulationTimeMillis;
    }

    @Nonnull
    public String getEndpoint() {
      return endpoint;
    }
  }

  // state
  private final String[] args;

  // parsed variables
  private long scanStreamDelay = DEFAULT_SCAN_STREAM_DELAY_MILLIS;
  private String stopFileName = null;
  private String sourceFileName;
  private long stopFilePollingDelayMillis = DEFAULT_STOP_FILE_POLLING_DELAY_MILLIS;
  private int maxStacktraceSize = DEFAULT_MAX_STACKTRACE_SIZE;
  private long maxStacktracePopulationTimeMillis = DEFAULT_MAX_STACKTRACE_POPULATION_TIME_MILLIS;
  private String endpoint = DEFAULT_ENDPOINT;


  public ArgParser(@Nonnull String[] args) {
    this.args = Objects.requireNonNull(args, "args");
  }

  public final int parse() {
    try {
      return doParse();
    } catch (IllegalStateException e) {
      System.err.println("Error: " + e + "\n");
      showHelp();
      return -1;
    }
  }

  @Nonnull
  public final Result getParseResult() {
    return new Result(scanStreamDelay, stopFileName, sourceFileName, stopFilePollingDelayMillis, maxStacktraceSize,
        maxStacktracePopulationTimeMillis, endpoint);
  }

  //
  // Private
  //

  @Nonnull
  private String stringArgValue(int pos, @Nonnull String valueName) {
    int nextPos = pos + 1;
    if (nextPos < args.length) {
      pos = nextPos;
      return args[pos];
    }
    throw new IllegalStateException("Extra argument expected for " + valueName);
  }

  private int intArgValue(int pos, @Nonnull String valueName) {
    final String arg = stringArgValue(pos, valueName);
    try {
      return Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Unable to parse " + valueName, e);
    }
  }

  private int doParse() {
    // try find help switch (position doesn't matter, it overrides anything)
    for (final String arg : args) {
      if ("--help".equals(arg) || "-h".equals(arg)) {
        showHelp();
        return 0;
      }
    }

    // parse arguments
    for (int pos = 0; pos < args.length; ++pos) {
      if (!parseCurrentArg(pos)) {
        showHelp();
        return -1;
      }
    }

    return 0;
  }

  protected boolean parseCurrentArg(int pos) {
    if ("-f".equals(args[pos]) || "--file".equals(args[pos])) {
      sourceFileName = stringArgValue(pos, "Source File Name");
    } else if ("-e".equals(args[pos]) || "--endpoint".equals(args[pos])) {
      endpoint = stringArgValue(pos, "Endpoint");
    } else if ("--scan-delay".equals(args[pos])) {
      scanStreamDelay = intArgValue(pos, "Scan Delay");
    } else if ("--stop-file-name".equals(args[pos])) {
      stopFileName = stringArgValue(pos, "Stop File Name");
    } else if ("--stop-file-polling-delay".equals(args[pos])) {
      stopFilePollingDelayMillis = intArgValue(pos, "Stop File Polling Delay");
    } else if ("--stop-file-polling-delay".equals(args[pos])) {
      stopFilePollingDelayMillis = intArgValue(pos, "Stop File Polling Delay");
    } else if ("--max-stacktrace-size".equals(args[pos])) {
      maxStacktraceSize = intArgValue(pos, "Max Stacktrace Size");
    } else if ("--max-stacktrace-population-time".equals(args[pos])) {
      maxStacktracePopulationTimeMillis = intArgValue(pos, "Max Stacktrace Population Time");
    }

    return true;
  }

  private void showHelp() {
    System.out.println("Usage:\n" +
        "--help,-h                  Show help.\n" +

        "--file,-f {STRING}         Source file name.\n" +
        "                           This is the required value, it should contain\n" +
        "                           a path to the log file to analyze.\n" +

        "--endpoint,-e {STRING}     Target camel endpoint.\n" +
        "                           This is the required value, it should conform to\n" +
        "                           Camel endpoint schemed, default value=" + DEFAULT_ENDPOINT + "\n" +

        "--scan-delay {NUMBER}      Time in milliseconds for scanning source file name for changes,\n" +
        "                           default value=" + DEFAULT_SCAN_STREAM_DELAY_MILLIS + '\n' +

        "--stop-file-name {STRING}  Stop file name, ignored by default.\n" +
        "                           The presence of this file will make application stop.\n" +

        "--stop-file-polling-delay {NUMBER} Delay before stop file probing iterations,\n" +
        "                           default value=" + DEFAULT_STOP_FILE_POLLING_DELAY_MILLIS + '\n' +

        "--max-stacktrace-size {NUMBER} Maximum number of lines in stacktrace,\n" +
        "                           default value=" + DEFAULT_MAX_STACKTRACE_SIZE + '\n' +

        "--max-stacktrace-population-time {NUMBER} Time to wait, in milliseconds,\n" +
        "                           to populate the complete stacktrace for logging statement,\n" +
        "                           default value=" + DEFAULT_MAX_STACKTRACE_SIZE + '\n' +

        "\n");
  }
}
