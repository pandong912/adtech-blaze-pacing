package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HwmAllocationDetail {
  private long adSetId;
  private double probability;

}
