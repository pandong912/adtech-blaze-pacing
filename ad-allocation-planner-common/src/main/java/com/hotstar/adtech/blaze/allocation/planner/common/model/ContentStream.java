package com.hotstar.adtech.blaze.allocation.planner.common.model;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder
public class ContentStream {
  //OFFSET is to avoid cohort concurrencyId and stream concurrencyId be duplicated.
  private static final int OFFSET = 200000;

  // internal id for supplies
  @NonFinal
  int concurrencyId;
  PlayoutStream playoutStream;
  String contentId;
  long concurrency;
  StreamType streamType;
  String playoutId;

  public void setConcurrencyId(int value) {
    concurrencyId = value + OFFSET;
  }

  public String getKey() {
    return playoutStream.getKey();
  }

}