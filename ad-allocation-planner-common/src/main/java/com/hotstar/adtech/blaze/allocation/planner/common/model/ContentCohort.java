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
  @NonFinal
  @Setter
  int concurrencyId;
  String contentId;
  String ssaiTag;
  PlayoutStream playoutStream;
  long concurrency;
  StreamType streamType;


  public String getKey() {
    return playoutStream.getKey() + "|" + ssaiTag;
  }
}
