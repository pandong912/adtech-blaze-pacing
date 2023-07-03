package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.ShaleSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.PlanInfo;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAdSetDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShalePlanWorker {
  private final DiagnosisService diagnosisService;
  private final QualificationExecutor qualificationExecutor;
  private final ShaleSolver solver;

  public List<ShaleSolveResult> generatePlans(ShalePlanContext shalePlanContext, PlanType planType) {
    List<GraphContext> graphContexts =
      qualificationExecutor.doQualification(planType, shalePlanContext.getGeneralPlanContext());

    return graphContexts.parallelStream()
      .map(graphContext -> solveGraph(shalePlanContext, graphContext))
      .collect(Collectors.toList());
  }


  private ShaleSolveResult solveGraph(ShalePlanContext shalePlanContext, GraphContext graphContext) {
    GeneralPlanContext generalPlanContext = shalePlanContext.getGeneralPlanContext();
    ReachStorage reachStorage = shalePlanContext.getReachStorage();
    List<ShaleResult> shaleResults = solver.solve(graphContext, reachStorage, shalePlanContext.getPenalty());

    List<DemandDiagnosis> demandDiagnosisList = generalPlanContext.getDemandDiagnosisList();

    List<ShaleAdSetDiagnosis> adSetDiagnoses =
      diagnosisService.getShaleAdSetDiagnosisList(demandDiagnosisList, shaleResults);
    PlanInfo planInfo = diagnosisService.buildPlanInfo(graphContext, generalPlanContext.getBreakContext());
    ShaleAllocationPlan shaleAllocationPlan =
      buildShaleAllocationResult(generalPlanContext, graphContext, shaleResults);
    return ShaleSolveResult.builder()
      .shaleAllocationPlan(shaleAllocationPlan)
      .shaleAllocationDiagnosisDetail(ShaleAllocationDiagnosisDetail.builder()
        .adSetDiagnoses(adSetDiagnoses)
        .planInfo(planInfo)
        .build())
      .build();
  }

  private ShaleAllocationDetail buildShaleAllocationDetail(ShaleResult shaleResult) {
    return ShaleAllocationDetail.builder()
      .adSetId(shaleResult.getId())
      .sigma(shaleResult.getSigma())
      .std(shaleResult.getStd())
      .mean(shaleResult.getMean())
      .alpha(shaleResult.getAlpha())
      .theta(shaleResult.getTheta())
      .reachEnabled(shaleResult.getReachEnabled())
      .adDuration(shaleResult.getAdDuration())
      .build();
  }

  public ShaleAllocationPlan buildShaleAllocationResult(GeneralPlanContext generalPlanContext,
                                                        GraphContext graphContext,
                                                        List<ShaleResult> shaleResults) {
    String contentId = generalPlanContext.getContentId();
    BreakContext breakContext = generalPlanContext.getBreakContext();
    return ShaleAllocationPlan.builder()
      .contentId(contentId)
      .breakTypeIds(graphContext.getBreakTypeGroup().getBreakTypeIds())
      .nextBreakIndex(breakContext.getNextBreakIndex())
      .totalBreakNumber(breakContext.getTotalBreakNumber())
      .duration(graphContext.getBreakDuration())
      .shaleAllocationDetails(
        shaleResults.stream().map(this::buildShaleAllocationDetail).collect(Collectors.toList()))
      .build();
  }
}
