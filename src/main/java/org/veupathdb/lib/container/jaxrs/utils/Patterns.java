package org.veupathdb.lib.container.jaxrs.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patterns
{
  public static Optional < String > group(
    final int index,
    final Matcher matcher
  ) {
    return matcher.find()
      ? Optional.of(matcher.group(index))
      : Optional.empty();
  }

  public static Optional < String > group(
    final int index,
    final Pattern pat,
    final String raw
  ) {
    return group(index, pat.matcher(raw));
  }

  public static Optional < String > firstGroup(final Matcher matcher) {
    return group(1, matcher);
  }

  public static Optional < String > firstGroup(
    final Pattern pat,
    final String raw
  ) {
    return group(1, pat.matcher(raw));
  }
}
