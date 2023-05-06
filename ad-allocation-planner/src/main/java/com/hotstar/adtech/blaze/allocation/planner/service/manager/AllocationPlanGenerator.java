package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;

public interface AllocationPlanGenerator {
  void generateAndUploadAllocationPlan(Match match, AdModel adModel);
}
