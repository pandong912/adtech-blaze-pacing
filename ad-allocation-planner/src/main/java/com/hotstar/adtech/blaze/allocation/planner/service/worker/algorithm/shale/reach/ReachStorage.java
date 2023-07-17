package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;

public interface ReachStorage {
  default double getTd(ShaleDemand demand, ShaleSupply supply) {
    if (demand.getReachEnabled() == 1) {
      double reachRatio = getUnReachRatio(demand.getId(), supply.getId());
      return demand.getTheta() + Math.max(0, reachRatio - demand.getReachOffset());
    } else {
      return demand.getTheta();
    }
  }

  default double getRd(ShaleDemand demand, ShaleSupply supply) {
    if (demand.getReachEnabled() == 1) {
      double reachRatio = getUnReachRatio(demand.getId(), supply.getId());
      return Math.min(1, Math.max(reachRatio - demand.getReachOffset(), 0) / (demand.getReachOffset() + 0.000001));
    } else {
      return 0;
    }
  }

  double getUnReachRatio(int demandId, int concurrencyId);
}

