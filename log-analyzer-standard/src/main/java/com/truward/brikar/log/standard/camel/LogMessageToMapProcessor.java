package com.truward.brikar.log.standard.camel;

import com.truward.brikar.log.model.LogMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.Map;

/**
 * A processor that transforms
 *
 * @author Alexander Shabanov
 */
public final class LogMessageToMapProcessor implements Processor {
  @Override
  public void process(Exchange exchange) throws Exception {
    final LogMessage logMessage = exchange.getIn().getBody(LogMessage.class);
    final Map<String, Object> map = new HashMap<>(30);

    map.put("lines", logMessage.getLines());
    map.put("severity", logMessage.getSeverity().toString());
    map.put("time", logMessage.getUnixTime());
    map.put("attributes", logMessage.getAttributes());

    exchange.getOut().setBody(map);
  }
}
