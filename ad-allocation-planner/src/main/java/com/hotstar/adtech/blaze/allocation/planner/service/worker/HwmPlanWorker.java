package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.HwmAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.HwmResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm.HwmSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SpotQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SsaiQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HwmPlanWorker {
  private final SsaiQualificationExecutor ssaiQualificationExecutor;
  private final SpotQualificationExecutor spotQualificationExecutor;
  private final HwmSolver solver;

  @Timed(value = MetricNames.PLAN_WORKER, extraTags = {"type", "hwm"})
  public HwmAllocationPlan generatePlans(GeneralPlanContext generalPlanContext, PlanType planType,
                                         List<Integer> breakTypeIds, Integer duration) {
    GraphContext graphContext = planType == PlanType.SSAI
      ? ssaiQualificationExecutor.executeQualify(generalPlanContext, breakTypeIds, duration) :
      spotQualificationExecutor.executeQualify(generalPlanContext, breakTypeIds, duration);
    List<HwmResult> hwmResults = solver.solve(graphContext);
    return buildHwmAllocationResult(generalPlanContext, graphContext, hwmResults);
  }

  public HwmAllocationPlan buildHwmAllocationResult(GeneralPlanContext generalPlanContext,
                                                    GraphContext graphContext, List<HwmResult> hwmAllocationResults) {
    String contentId = generalPlanContext.getContentId();
    BreakContext breakContext = generalPlanContext.getBreakContext();
    return HwmAllocationPlan.builder()
      .planType(graphContext.getPlanType())
      .contentId(contentId)
      .nextBreakIndex(breakContext.getNextBreakIndex())
      .totalBreakNumber(breakContext.getTotalBreakNumber())
      .breakTypeIds(graphContext.getBreakTypeIds())
      .duration(graphContext.getBreakDuration())
      .hwmAllocationDetails(
        hwmAllocationResults.stream().map(this::buildHwmAllocationDetail).collect(Collectors.toList()))
      .build();
  }

  private HwmAllocationDetail buildHwmAllocationDetail(HwmResult hwmResult) {
    return HwmAllocationDetail.builder()
      .adSetId(hwmResult.getId())
      .probability(hwmResult.getProbability())
      .build();
  }
}
