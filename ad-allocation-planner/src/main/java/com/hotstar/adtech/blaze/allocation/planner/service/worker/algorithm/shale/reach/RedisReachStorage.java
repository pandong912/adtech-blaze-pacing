package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach;

import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisReachStorage implements ReachStorage {
  private final double[][] unReachRatio;

  @Override
  public double getUnReachRatioFromStorage(int demandId, int concurrencyId) {
    return unReachRatio[demandId][concurrencyId];
  }
}

