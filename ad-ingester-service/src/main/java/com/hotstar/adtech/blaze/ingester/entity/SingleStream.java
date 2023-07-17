package com.hotstar.adtech.blaze.ingester.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SingleStream {
  static final String SPLITTER = "-";
  String tenant;

  String language;

  String ladder;

  String ads;

  String playoutId;

  public String getKey() {
    return tenant + SPLITTER + language + SPLITTER + ladder + SPLITTER + ads;
  }
}
