package com.hotstar.adtech.blaze.allocationdata.client.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShalePlanContext {
  GeneralPlanContext generalPlanContext;
  ReachStorage reachStorage;
  double penalty;
}
