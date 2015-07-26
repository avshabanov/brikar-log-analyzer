package com.truward.brikar.log.route;

import com.truward.brikar.log.camel.MalformedLineFilter;
import com.truward.brikar.log.camel.MalformedLogMessageFilter;
import com.truward.brikar.log.camel.MultiLineAggregationStrategy;
import com.truward.brikar.log.model.LogMessage;
import com.truward.brikar.log.model.MaterializedLogMessage;
import com.truward.brikar.log.model.Severity;
import com.truward.brikar.log.util.TestLogMessageProcessor;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Tests aggregating message route.
 *
 * @author Alexander Shabanov
 */
public final class AggregatingMessageRouteTest extends CamelTestSupport {

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  private static final String TEST_LOG_CHUNK = "2015-07-24 23:21:16,942 INFO learn.LogProducerMain  " +
      "[learn.LogProducerMain.main()] Operation timed out\n" +
      "2015-07-24 23:39:55,002 WARN learn.LogProducerMain oid=pg/BBY//9YgCHcJn, rid=anCYuTwUWbtnuZp1 " +
      "[learn.LogProducerMain.main()] Error operation #0 attempted\n" +
      "java.lang.UnsupportedOperationException: This operation is not supported yet\n" +
      "\tat learn.LogProducerMain.throwUnsupportedOperationException(LogProducerMain.java:149) [classes/:na]\n" +
      "\tat learn.LogProducerMain.doErrorOperation(LogProducerMain.java:145) [classes/:na]\n" +
      "\tat learn.LogProducerMain.repl(LogProducerMain.java:127) [classes/:na]\n" +
      "\tat learn.LogProducerMain.run(LogProducerMain.java:49) [classes/:na]\n" +
      "\tat learn.LogProducerMain.main(LogProducerMain.java:32) [classes/:na]\n" +
      "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_45]\n" +
      "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_45]\n" +
      "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_45]\n" +
      "\tat java.lang.reflect.Method.invoke(Method.java:497) ~[na:1.8.0_45]\n" +
      "\tat org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:297) [exec-maven-plugin-1.2.1.jar:na]\n" +
      "\tat java.lang.Thread.run(Thread.java:745) [na:1.8.0_45]\n" +
      "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
      "rid=KhnHxNK/BbLbaiH4 " +
      "[learn.LogProducerMain.main()] Disk full\n";


  @Test
  public void shouldGetThreeMessages() throws InterruptedException {
    // Given:
    resultEndpoint.expectedMessageCount(3);

    // When:
    template.sendBody(TEST_LOG_CHUNK.getBytes(StandardCharsets.UTF_8));

    // Then:
    resultEndpoint.assertIsSatisfied();
    final LogMessage message1 = resultEndpoint.assertExchangeReceived(0).getIn().getBody(LogMessage.class);
    final LogMessage message2 = resultEndpoint.assertExchangeReceived(1).getIn().getBody(LogMessage.class);
    final LogMessage message3 = resultEndpoint.assertExchangeReceived(2).getIn().getBody(LogMessage.class);

    assertTrue(message1 instanceof MaterializedLogMessage);
    assertTrue(message2 instanceof MaterializedLogMessage);
    assertTrue(message3 instanceof MaterializedLogMessage);

    assertEquals(Severity.INFO, message1.getSeverity());
    assertEquals(Severity.WARN, message2.getSeverity());
    assertEquals(Severity.ERROR, message3.getSeverity());
  }

  @Override
  protected RouteBuilder createRouteBuilder() {
    return new RouteBuilder() {
      public void configure() {
        from("direct:start")
            .split(body(String.class).regexTokenize("\n"))
            .filter(new MalformedLineFilter())
            .process(new TestLogMessageProcessor())
            .aggregate(header("id"), new MultiLineAggregationStrategy()).completionInterval(200L)
            .filter(new MalformedLogMessageFilter())
            .to("mock:result");
      }
    };
  }
}
