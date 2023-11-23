package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach;

import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DegradationReachStorage implements ReachStorage {

  @Override
  public double getUnReachRatioFromStorage(int demandId, int concurrencyId) {
    return 0;
  }
}

