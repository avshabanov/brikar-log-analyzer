package com.truward.brikar.log.util;

import com.truward.brikar.log.model.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message processor that converts a line into a message.
 *
 * @author Alexander Shabanov
 */
@Ignore
public final class TestLogMessageProcessor implements Processor {

  public static final Pattern RECORD_PATTERN = Pattern.compile(
      "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) " + // date+time
          "(\\p{Upper}+) " + // severity
          "([\\w\\.\\$]+) " + // class name
          "((?:[\\w]+=[\\w\\+/\\.\\$]+)(?:, (?:[\\w]+=[\\w\\+/\\.\\$]+))*)? " + // variables
          "\\[[\\w\\.\\$\\(\\)]+\\] " + // thread ID
          "(.+)" + // message
          "$"
  );

  private static final String METRIC_MARKER = "@metric ";

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final DateFormat dateFormat;
  private int count = 0;

  public TestLogMessageProcessor() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    final String line = exchange.getIn().getBody(String.class);
    final LogMessage logMessage = parse(line);

    exchange.getOut().setBody(logMessage);

    // create header that will assign unique ID - this assumes message will come in the same order as they
    // appear in the source log file - otherwise this code will not work
    if (!logMessage.isMultiLinePart()) {
      ++count;
    }
    exchange.getOut().setHeader("id", count);
  }

  // visible for testing
  public LogMessage parse(String line) {
    final Matcher matcher = RECORD_PATTERN.matcher(line);
    if (!matcher.matches()) {
      return new MultiLinePartLogMessage(line);
    }

    if (matcher.groupCount() < 5) {
      log.error("Count of groups is not six: actual={} for line={}", matcher.groupCount(), line);
      return NullLogMessage.INSTANCE; // should not happen
    }

    final Date date;
    try {
      date = dateFormat.parse(matcher.group(1));
    } catch (ParseException e) {
      log.error("Malformed date in line={}", line, e);
      return NullLogMessage.INSTANCE; // should not happen
    }

    final Severity severity = Severity.fromString(matcher.group(2), Severity.WARN);

    final MaterializedLogMessage logMessage = new MaterializedLogMessage(date.getTime(), severity, line);
    addAttributesFromVariables(logMessage, matcher.group(4));

    final String message = matcher.group(5);
    if (message != null) {
      final int metricIndex = message.indexOf(METRIC_MARKER);
      if (metricIndex >= 0) {
        addAttributesFromMetrics(logMessage, message.substring(metricIndex + METRIC_MARKER.length()));
      }
    }

    return logMessage;
  }

  //
  // Private
  //

  private void addAttributesFromMetrics(MaterializedLogMessage logMessage, String metricBody) {
    putAllAttributes(logMessage, new CommaSeparatedValueParser(metricBody).readAsMap());
  }

  private void addAttributesFromVariables(MaterializedLogMessage logMessage, String variables) {
    if (variables != null) {
      putAllAttributes(logMessage, new CommaSeparatedValueParser(variables).readAsMap());
    }
  }

  private void putAllAttributes(MaterializedLogMessage logMessage, Map<String, String> vars) {
    for (final Map.Entry<String, String> entry : vars.entrySet()) {
      logMessage.putAttribute(entry.getKey(), entry.getValue());
    }
  }
}
