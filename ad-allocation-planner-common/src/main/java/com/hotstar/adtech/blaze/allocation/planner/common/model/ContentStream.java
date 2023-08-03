package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder
public class ContentStream {
  // internal id for supplies
  // concurrencyId should be same as the index in the List<ContentStream>!
  @NonFinal
  @Setter
  int concurrencyId;
  PlayoutStream playoutStream;
  String contentId;
  long concurrency;

  public String getKey() {
    return playoutStream.getKey();
  }

}
