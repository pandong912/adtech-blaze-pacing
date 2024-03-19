package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder
public class ContentStream {
  // internal id for supplies
  // concurrencyId should be same as the index in the List<ContentStream>!
  @NonFinal
  int concurrencyIdInStream;
  @NonFinal
  int concurrencyIdInCohort;
  PlayoutStream playoutStream;
  String contentId;
  long concurrency;

  public void setConcurrencyId(int i, int cohortSize) {
    this.concurrencyIdInStream = i;
    this.concurrencyIdInCohort = i + cohortSize;
  }

}