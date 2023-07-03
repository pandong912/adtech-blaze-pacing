package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;

public interface ReachStorage {
  default double getTd(ShaleDemand demand, ShaleSupply supply) {
    double reachRatio = getUnReachRatio(demand.getId(), supply.getId());
    return demand.getTheta() + demand.getReachEnabled() * Math.max(0, reachRatio - demand.getReachOffset());
  }

  default double getRd(ShaleDemand demand, ShaleSupply supply) {
    double reachRatio = getUnReachRatio(demand.getId(), supply.getId());
    return demand.getReachEnabled()
      * (Math.min(1, Math.max(reachRatio - demand.getReachOffset(), 0) / (demand.getReachOffset() + 0.000001)));
  }

  double getUnReachRatio(int demandId, int concurrencyId);
}

