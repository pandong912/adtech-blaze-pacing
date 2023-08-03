package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.HwmAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.HwmSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAdSetDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.PlanInfo;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.HwmResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm.HwmSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HwmPlanWorker {
  private final DiagnosisService diagnosisService;
  private final QualificationExecutor qualificationExecutor;
  private final HwmSolver solver;

  @Timed(value = MetricNames.WORKER, extraTags = {"type", "hwm"})
  public List<HwmSolveResult> generatePlans(GeneralPlanContext generalPlanContext, PlanType planType) {
    List<GraphContext> graphContexts =
      qualificationExecutor.doQualification(planType, generalPlanContext);
    return graphContexts.parallelStream()
      .map(graphContext -> solveGraph(generalPlanContext, graphContext))
      .collect(Collectors.toList());
  }

  private HwmSolveResult solveGraph(GeneralPlanContext generalPlanContext, GraphContext graphContext) {
    List<HwmResult> hwmResults = solver.solve(graphContext);

    List<DemandDiagnosis> demandDiagnosisList = generalPlanContext.getDemandDiagnosisList();

    List<HwmAdSetDiagnosis> adSetDiagnoses =
      diagnosisService.getHwmAdSetDiagnosisList(demandDiagnosisList, hwmResults);
    PlanInfo planInfo = diagnosisService.buildPlanInfo(graphContext, generalPlanContext.getBreakContext());
    HwmAllocationPlan hwmAllocationPlan = buildHwmAllocationResult(generalPlanContext, graphContext, hwmResults);
    return HwmSolveResult.builder()
      .hwmAllocationDiagnosisDetail(HwmAllocationDiagnosisDetail.builder()
        .planInfo(planInfo)
        .adSetDiagnoses(adSetDiagnoses)
        .build())
      .hwmAllocationPlan(hwmAllocationPlan)
      .build();
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
      .breakTypeIds(graphContext.getBreakTypeGroup().getBreakTypeIds())
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
