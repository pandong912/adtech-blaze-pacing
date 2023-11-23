package com.hotstar.adtech.blaze.allocationdata.client.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BreakContext {
  private final int nextBreakIndex;
  private final int totalBreakNumber;
  private final int estimatedModelBreakIndex;
  private final double expectedRatio;
  private final double expectedProgress;
}
