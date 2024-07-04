package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import java.util.stream.Collectors;

public class AllocationBuilder {
  public static AllocationPlan getHwmAllocationPlan(String contentId, AllocationPlanResult result,
                                                    GeneralPlanContext generalPlanContext,
                                                    Long planId, HwmAllocationPlan plan) {
    return AllocationPlan.builder()
      .planType(plan.getPlanType().toString())
      .breakDuration(plan.getDuration())
      .breakType(plan.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
      .estimatedModelBreakIndex(generalPlanContext.getBreakContext().getEstimatedModelBreakIndex())
      .totalBreakNumber(plan.getTotalBreakNumber())
      .expectedProgress(generalPlanContext.getBreakContext().getExpectedProgress())
      .expectedRatio(generalPlanContext.getBreakContext().getExpectedRatio())
      .nextBreakIndex(plan.getNextBreakIndex())
      .algorithmType(AlgorithmType.HWM.toString())
      .planId(planId)
      .siMatchId(contentId)
      .version(result.getVersion())
      .build();
  }

  public static AllocationPlan getShaleAllocationPlan(String contentId, AllocationPlanResult result,
                                                      GeneralPlanContext generalPlanContext,
                                                      Long planId, ShaleAllocationPlan plan) {
    return AllocationPlan.builder()
      .planType(PlanType.SSAI.toString())
      .breakDuration(plan.getDuration())
      .breakType(plan.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
      .estimatedModelBreakIndex(generalPlanContext.getBreakContext().getEstimatedModelBreakIndex())
      .totalBreakNumber(plan.getTotalBreakNumber())
      .expectedProgress(generalPlanContext.getBreakContext().getExpectedProgress())
      .expectedRatio(generalPlanContext.getBreakContext().getExpectedRatio())
      .nextBreakIndex(plan.getNextBreakIndex())
      .algorithmType(AlgorithmType.SHALE.toString())
      .planId(planId)
      .siMatchId(contentId)
      .version(result.getVersion())
      .build();
  }
}

