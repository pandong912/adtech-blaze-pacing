package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationPlan;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;

public class AllocationBuilder {
  public static AllocationPlan getAllocationPlan(String contentId, AllocationPlanResult result,
                                                 GeneralPlanContext generalPlanContext,
                                                 AllocationPlanResultDetail detail) {
    return AllocationPlan.builder()
      .planType(detail.getPlanType().toString())
      .breakDuration(detail.getDuration())
      .breakType(detail.getBreakTypeIds())
      .estimatedModelBreakIndex(generalPlanContext.getBreakContext().getEstimatedModelBreakIndex())
      .totalBreakNumber(detail.getTotalBreakNumber())
      .expectedProgress(generalPlanContext.getBreakContext().getExpectedProgress())
      .expectedRatio(generalPlanContext.getBreakContext().getExpectedRatio())
      .nextBreakIndex(detail.getNextBreakIndex())
      .algorithmType(detail.getAlgorithmType().toString())
      .planId(detail.getId())
      .siMatchId(contentId)
      .version(result.getVersion())
      .build();
  }
}

