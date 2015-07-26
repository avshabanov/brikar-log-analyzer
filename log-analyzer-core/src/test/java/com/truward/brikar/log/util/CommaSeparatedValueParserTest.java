package com.truward.brikar.log.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link com.truward.brikar.log.util.CommaSeparatedValueParser}.
 *
 * @author Alexander Shabanov
 */
public final class CommaSeparatedValueParserTest {

  @Test
  public void shouldParseSingleEntry() {
    // Given:
    final CommaSeparatedValueParser parser = new CommaSeparatedValueParser("key=value");

    // When:
    final String key = parser.nextKey();
    final String value = parser.nextValue();

    // Then:
    assertNull(parser.nextKey());
    assertEquals("key", key);
    assertEquals("value", value);
  }

  @Test
  public void shouldReadAsMap() {
    // Given:
    final CommaSeparatedValueParser parser = new CommaSeparatedValueParser("a=12, b=3, cc=4");

    // When:
    final Map<String, String> map = parser.readAsMap();

    // Then:
    final Map<String, String> expected = new HashMap<>();
    expected.put("a", "12");
    expected.put("b", "3");
    expected.put("cc", "4");
    assertEquals(expected, map);
  }

  @Test
  public void shouldReadAsMapIgnoringTrailingWhitespace() {
    // Given:
    final CommaSeparatedValueParser parser1 = new CommaSeparatedValueParser("a=12, b=3, cc=4");
    final CommaSeparatedValueParser parser2 = new CommaSeparatedValueParser("a=12, b=3, cc=4  ");

    // When:
    final Map<String, String> map1 = parser1.readAsMap();
    final Map<String, String> map2 = parser2.readAsMap();

    // Then:
    assertEquals(map1, map2);
  }
}
