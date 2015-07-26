package com.truward.brikar.log.standard;

import com.truward.brikar.log.camel.MalformedLineFilter;
import com.truward.brikar.log.camel.MalformedLogMessageFilter;
import com.truward.brikar.log.model.LogMessage;
import com.truward.brikar.log.model.Severity;
import com.truward.brikar.log.standard.camel.LogMessageProcessor;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Tests how router works with incoming messages.
 *
 * @author Alexander Shabanov
 */
public final class NonAggregatingMessageRouteTest extends CamelTestSupport {

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Test
  public void shouldGetTwoMessages() throws InterruptedException {
    // Given:
    resultEndpoint.expectedMessageCount(2);

    // When:
    template.sendBody(("2015-07-24 23:21:16,942 WARN learn.LogProducerMain  " +
        "[learn.LogProducerMain.main()] Operation timed out\n" +
        "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
        "rid=KhnHxNK/BbLbaiH4 " +
        "[learn.LogProducerMain.main()] Disk full\n"
    ).getBytes(StandardCharsets.UTF_8));

    // Then:
    resultEndpoint.assertIsSatisfied();
    final LogMessage message1 = resultEndpoint.assertExchangeReceived(0).getIn().getBody(LogMessage.class);
    final LogMessage message2 = resultEndpoint.assertExchangeReceived(1).getIn().getBody(LogMessage.class);

    assertEquals(1437780076942L, message1.getUnixTime());

    assertEquals(Severity.WARN, message1.getSeverity());
    assertEquals(Severity.ERROR, message2.getSeverity());
  }

  @Override
  protected RouteBuilder createRouteBuilder() {
    return new RouteBuilder() {
      public void configure() {
        from("direct:start")
            .split(body(String.class).regexTokenize("\n"))
            .filter(new MalformedLineFilter())
            .process(new LogMessageProcessor())
            .filter(new MalformedLogMessageFilter())
            .to("mock:result");
      }
    };
  }
}
