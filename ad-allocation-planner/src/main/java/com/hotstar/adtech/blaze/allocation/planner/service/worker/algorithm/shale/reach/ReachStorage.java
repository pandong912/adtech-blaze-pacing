package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;

public interface ReachStorage {
  default double getTd(ShaleDemand demand, ShaleSupply supply) {
    double reachRatio = getUnReachRatio(demand, supply);
    return demand.getTheta() + Math.max(0, reachRatio - demand.getReachOffset());
  }

  default double getRd(ShaleDemand demand, ShaleSupply supply) {
    double reachRatio = getUnReachRatio(demand, supply);
    return Math.min(1, Math.max(reachRatio - demand.getReachOffset(), 0) / (demand.getReachOffset() + 0.000001));
  }

  default double getUnReachRatio(ShaleDemand demand, ShaleSupply supply) {
    if (demand.getReachEnabled() == 1 && supply.getStreamType() == StreamType.SSAI_Spot) {
      double reachRatio = getUnReachRatioFromStorage(demand.getId(), supply.getId());
      return Math.min(1, Math.max(reachRatio - demand.getReachOffset(), 0) / (demand.getReachOffset() + 0.000001));
    } else {
      return 0;
    }
  }

  double getUnReachRatioFromStorage(int demandId, int concurrencyId);
}

