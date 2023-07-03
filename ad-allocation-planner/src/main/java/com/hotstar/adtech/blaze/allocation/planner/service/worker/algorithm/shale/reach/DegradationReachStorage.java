package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DegradationReachStorage implements ReachStorage {
  @Override
  public double getTd(ShaleDemand demand, ShaleSupply supply) {
    return demand.getTheta();
  }

  @Override
  public double getRd(ShaleDemand demand, ShaleSupply supply) {
    return 0;
  }

  @Override
  public double getUnReachRatio(int demandId, int concurrencyId) {
    return 0;
  }
}

