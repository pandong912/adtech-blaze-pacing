package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder
public class ContentCohort {
  // internal id for supplies
  // concurrencyId should be same as the index in the List<ContentCohort>!
  @NonFinal
  @Setter
  int concurrencyId;
  String contentId;
  String ssaiTag;
  PlayoutStream playoutStream;
  long concurrency;

  public String getPlayoutIdKey() {
    return playoutStream.getPlayoutId() + "|" + ssaiTag;
  }
}
