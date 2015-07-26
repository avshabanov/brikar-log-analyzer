package com.truward.brikar.log.standard;

import com.truward.brikar.log.camel.MalformedLineFilter;
import com.truward.brikar.log.camel.MalformedLogMessageFilter;
import com.truward.brikar.log.camel.MultiLineAggregationStrategy;
import com.truward.brikar.log.standard.camel.LogMessageProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.File;

/**
 * @author Alexander Shabanov
 */
public class Main {
  private static final String STOP_FILE = "/tmp/stop_logger";

  public static void main(String[] args) throws Exception {
    final File stopFile = new File(STOP_FILE);

    final DefaultCamelContext context = new DefaultCamelContext();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if (context.isStoppingOrStopped()) {
          return;
        }

        System.out.println("Shutting down context...");
        try {
          context.stop();
        } catch (Exception ignored) {
          // suppress
        }
      }
    });

    context.addRoutes(new MainRouteBuilder());

    try {
      context.start();

      while (!stopFile.exists()) {
        Thread.sleep(1000L);
      }
    } finally {
      context.stop();
    }
  }

  //
  // Private
  //

  private static final class MainRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
      from("stream:file?fileName=/tmp/slp.log&scanStream=true&scanStreamDelay=100") // analog of UNIX tail
          .split(body(String.class).regexTokenize("\n"))
          .filter(new MalformedLineFilter())
          .process(new LogMessageProcessor())
          .aggregate(new MultiLineAggregationStrategy()).header("id").completionSize(10000).completionInterval(200L)
          .filter(new MalformedLogMessageFilter())
          .to("stream:file?fileName=/dev/stdout")
      ;
    }
  }
}
