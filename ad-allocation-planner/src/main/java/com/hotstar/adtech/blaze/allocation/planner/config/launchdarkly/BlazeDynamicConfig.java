package com.hotstar.adtech.blaze.allocation.planner.config.launchdarkly;

import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationPlanMode;

public interface BlazeDynamicConfig {

  Boolean getEnableMaximiseReach();

  AllocationPlanMode getAllocationPlanMode();
}
