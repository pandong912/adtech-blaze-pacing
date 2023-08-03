package com.hotstar.adtech.blaze.allocation.planner.common.model;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
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
  StreamType streamType;
  String playoutId;


  public String getKey() {
    return playoutStream.getKey() + "|" + ssaiTag;
  }

  public String getPlayoutIdKey() {
    return playoutId + "|" + ssaiTag;
  }
}
