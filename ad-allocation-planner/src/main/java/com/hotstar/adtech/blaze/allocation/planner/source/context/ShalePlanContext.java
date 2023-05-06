package com.hotstar.adtech.blaze.allocation.planner.source.context;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShalePlanContext {
  GeneralPlanContext generalPlanContext;
  double penalty;
}
