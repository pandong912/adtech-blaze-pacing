package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class HwmResult {
  private long id;
  private double probability;
}
