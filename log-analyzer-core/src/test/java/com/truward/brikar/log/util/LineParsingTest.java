package com.truward.brikar.log.util;

import com.truward.brikar.log.model.LogMessage;
import com.truward.brikar.log.model.Severity;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LineParsingTest {

  private static final String MSG1 = "2015-07-24 23:21:16,942 INFO learn.LogProducerMain " +
          "oid=aJ0JLwgnBlw7+tbZ, rid=8tYCTFqDZfXJEzgD " +
          "[learn.LogProducerMain.main()] Application started with args=[]";

  private static final String MSG2 = "2015-07-24 23:22:20,748 WARN learn.LogProducerMain  " +
          "[learn.LogProducerMain.main()] Operation timed out";

  private static final String MSG3 = "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
          "rid=KhnHxNK/BbLbaiH4 " +
          "[learn.LogProducerMain.main()] Disk full";

  private static final String MSG4 = "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
          "rid=1, oid=2, lc=3 " +
          "[learn.LogProducerMain.main()] Disk full";

  private static final String MSG5 = "2015-07-24 23:22:20,748 INFO learn.LogProducerMain " +
      "rid=KhnHxNK/BbLbaiH4 " +
      "[learn.LogProducerMain.main()] @metric tDelta=545, op=UserService.getUserById";

  private final TestLogMessageProcessor processor = new TestLogMessageProcessor();

  @Test
  public void shouldMatchMsg1() {
    final LogMessage logMessage = processor.parse(MSG1);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(MSG1, logMessage.getLogEntry());
    assertEquals(1437780076942L, logMessage.getUnixTime());
    assertEquals(Collections.singletonList(MSG1), logMessage.getLines());
    assertEquals(2, logMessage.getAttributes().size());
    assertEquals("aJ0JLwgnBlw7+tbZ", logMessage.getAttributes().get("oid"));
    assertEquals("8tYCTFqDZfXJEzgD", logMessage.getAttributes().get("rid"));
  }

  @Test
  public void shouldMatchMsg2() {
    final LogMessage logMessage = processor.parse(MSG2);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.WARN, logMessage.getSeverity());
    assertEquals(MSG2, logMessage.getLogEntry());
    assertTrue(logMessage.getAttributes().isEmpty());
  }

  @Test
  public void shouldMatchMsg3() {
    final LogMessage logMessage = processor.parse(MSG3);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.ERROR, logMessage.getSeverity());
    assertEquals(MSG3, logMessage.getLogEntry());
    assertEquals(null, logMessage.getAttributes().get("oid"));
    assertEquals("KhnHxNK/BbLbaiH4", logMessage.getAttributes().get("rid"));
  }

  @Test
  public void shouldMatchMsg4() {
    final LogMessage logMessage = processor.parse(MSG4);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.ERROR, logMessage.getSeverity());
    assertEquals(MSG4, logMessage.getLogEntry());
    assertEquals("1", logMessage.getAttributes().get("rid"));
    assertEquals("2", logMessage.getAttributes().get("oid"));
    assertEquals("3", logMessage.getAttributes().get("lc"));
  }

  @Test
  public void shouldMatchMsg5() {
    final LogMessage logMessage = processor.parse(MSG5);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(MSG5, logMessage.getLogEntry());

    assertEquals("UserService.getUserById", logMessage.getAttributes().get("op"));
    assertEquals("545", logMessage.getAttributes().get("tDelta"));
    assertEquals("KhnHxNK/BbLbaiH4", logMessage.getAttributes().get("rid"));
  }
}
