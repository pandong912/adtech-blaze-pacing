package com.hotstar.adtech.blaze.allocation.planner.source.context;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShalePlanContext {
  GeneralPlanContext generalPlanContext;
  ReachStorage reachStorage;
  double penalty;
  Map<String, Integer> supplyIdMap;
}
