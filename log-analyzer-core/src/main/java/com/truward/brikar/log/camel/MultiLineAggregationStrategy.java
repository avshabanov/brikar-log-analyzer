package com.truward.brikar.log.camel;

import com.truward.brikar.log.model.LogMessage;
import com.truward.brikar.log.model.NullLogMessage;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregation strategy that folds incoming multipart messages
 * into {@link com.truward.brikar.log.model.MaterializedLogMessage}.
 *
 * @author Alexander Shabanov
 */
public final class MultiLineAggregationStrategy implements AggregationStrategy {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
    if (oldExchange == null) {
      return newExchange;
    }

    // get message bodies
    final LogMessage oldMsg = oldExchange.getIn().getBody(LogMessage.class);
    final LogMessage newMsg = newExchange.getIn().getBody(LogMessage.class);

    if (oldMsg.isMultiLinePart() || oldMsg.isNull()) {
      // override body to null if old message is multiline part or null
      log.error("Contract violation: old message is null or partial. oldMsg={}, newMsg={}", oldMsg, newMsg);
      newExchange.getIn().setBody(NullLogMessage.INSTANCE);
      return newExchange;
    }

    if (newMsg.isNull()) {
      // throw new message if it is null
      oldExchange.setProperty(Exchange.AGGREGATION_COMPLETE_CURRENT_GROUP, true);
      return oldExchange;
    }

    // append new message to the old one in a form of new line
    oldMsg.addLine(newMsg.getLogEntry());
    oldExchange.getIn().setBody(oldMsg);
    return oldExchange;
  }
}
