package com.bca.medisync.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

  public static Date parseIsoToDate(String iso) {
    if (iso == null) return null;
    try {
      OffsetDateTime odt = OffsetDateTime.parse(iso);
      return Date.from(odt.toInstant());
    } catch (Exception e) {
      return null;
    }
  }

  public static String format(String iso, String pattern) {
    if (iso == null) return "";
    try {
      OffsetDateTime odt = OffsetDateTime.parse(iso);
      ZonedDateTime local = odt.atZoneSameInstant(ZoneId.systemDefault());
      return local.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
    } catch (Exception e) {
      return iso;
    }
  }
}
