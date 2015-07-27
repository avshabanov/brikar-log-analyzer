package com.truward.brikar.log.standard;

import com.truward.brikar.log.camel.MalformedLineFilter;
import com.truward.brikar.log.camel.MalformedLogMessageFilter;
import com.truward.brikar.log.camel.MultiLineAggregationStrategy;
import com.truward.brikar.log.standard.camel.LogMessageProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Entry point.
 *
 * @author Alexander Shabanov
 */
public final class Main {

  public static void main(String[] args) throws Exception {
    final ArgParser argParser = new ArgParser(args);
    final int argParseResult = argParser.parse();
    if (argParseResult < 0) {
      System.exit(argParser.parse());
      return;
    }

    start(argParser.getParseResult());
  }

  //
  // Private
  //

  private Main() {} // Hidden ctor

  private static void start(@Nonnull ArgParser.Result args) throws Exception {
    final File stopFile = args.getStopFileName() != null ? new File(args.getStopFileName()) : null;

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

    context.addRoutes(new MainRouteBuilder(args.getScanStreamDelay(), args.getSourceFileName(),
        args.getMaxStacktraceSize(), args.getMaxStacktracePopulationTimeMillis()));

    try {
      context.start();

      while (stopFile == null || !stopFile.exists()) {
        Thread.sleep(args.getStopFilePollingDelayMillis());
      }
    } finally {
      context.stop();
    }
  }

  private static final class MainRouteBuilder extends RouteBuilder {
    private final long scanDelay;
    private final String fileName;
    private final int maxStacktraceSize;
    private final long maxStacktracePopulationTime;

    public MainRouteBuilder(long scanDelay, String fileName, int maxStacktraceSize, long maxStacktracePopulationTime) {
      this.scanDelay = scanDelay;
      this.fileName = fileName;
      this.maxStacktraceSize = maxStacktraceSize;
      this.maxStacktracePopulationTime = maxStacktracePopulationTime;
    }

    @Override
    public void configure() throws Exception {
      from("stream:file?fileName=" + fileName + "&scanStream=true&scanStreamDelay=" + scanDelay)
          .split(body(String.class).regexTokenize("\n"))
          .filter(new MalformedLineFilter())
          .process(new LogMessageProcessor())

          .aggregate(new MultiLineAggregationStrategy())
          .header("id").completionSize(maxStacktraceSize).completionInterval(maxStacktracePopulationTime)

          .filter(new MalformedLogMessageFilter())
          .to("stream:file?fileName=/dev/stdout")
      ;
    }
  }
}
