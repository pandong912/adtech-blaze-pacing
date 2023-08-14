package com.hotstar.adtech.blaze.allocation.planner.controller;

import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleResponse {
  List<ShaleAllocationPlan> allocationPlans;
  Map<String, Integer> concurrencyIdMap;
}
