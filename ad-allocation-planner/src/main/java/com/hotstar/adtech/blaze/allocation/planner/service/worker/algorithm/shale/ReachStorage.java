package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReachStorage {
  //Map<concurrencyId, Map<adSetId, reachRatio>>
  //  private final Map<Long, Map<Long, Double>> unReachRatio;
  private final double[][] unReachRatio;

  public double getUnReachRatio(int demandId, int concurrencyId) {
    //    return unReachRatio.getOrDefault(concurrencyId, Collections.emptyMap()).getOrDefault(adSetId, 1d);
    return unReachRatio[demandId][concurrencyId];
  }
}

