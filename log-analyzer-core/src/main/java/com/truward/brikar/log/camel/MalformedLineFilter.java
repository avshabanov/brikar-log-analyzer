package com.truward.brikar.log.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

/**
 * A camel filter that throws away empty lines.
 *
 * @author Alexander Shabanov
 */
public final class MalformedLineFilter implements Predicate {

  @Override
  public boolean matches(Exchange exchange) {
    final String line = exchange.getIn().getBody(String.class);
    return line.length() > 0;
  }
}
