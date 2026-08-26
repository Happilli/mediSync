package com.bca.medisync.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

  public static Date parseIsoToDate(String iso) {
    if (iso == null) return null;
    try {
      OffsetDateTime odt = OffsetDateTime.parse(iso);
      return Date.from(odt.toInstant());
    } catch (DateTimeParseException e) {
      try {
        // Fallback for formats without offset like "2024-05-25T10:30:00" or "2024-05-25 10:30:00"
        String cleanedIso = iso.replace(" ", "T");
        LocalDateTime ldt = LocalDateTime.parse(cleanedIso);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
      } catch (Exception ex) {
        return null;
      }
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
    } catch (DateTimeParseException e) {
      try {
        String cleanedIso = iso.replace(" ", "T");
        LocalDateTime ldt = LocalDateTime.parse(cleanedIso);
        ZonedDateTime local = ldt.atZone(ZoneId.systemDefault());
        return local.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
      } catch (Exception ex) {
        return iso;
      }
    } catch (Exception e) {
      return iso;
    }
  }
}
