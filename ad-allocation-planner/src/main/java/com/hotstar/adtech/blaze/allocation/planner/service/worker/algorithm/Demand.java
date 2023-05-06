package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import lombok.Getter;

@Getter
public class Demand {
  private final Integer id;
  private final double demand;
  private final int adDuration;

  public Demand(Integer id, double demand, int adDuration) {
    this.id = id;
    this.demand = demand;
    this.adDuration = adDuration;
  }
}
