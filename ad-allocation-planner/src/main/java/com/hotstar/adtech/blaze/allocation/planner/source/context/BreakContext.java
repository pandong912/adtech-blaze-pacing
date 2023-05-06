package com.hotstar.adtech.blaze.allocation.planner.source.context;

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
