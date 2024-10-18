package com.hotstar.adtech.blaze.allocation.planner.launchdarkly;

import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationPlanMode;

public interface DynamicConfig {

  Boolean getEnableMaximiseReach();

  AllocationPlanMode getAllocationPlanMode();
}
