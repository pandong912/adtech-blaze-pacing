package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.AD_SET_DIAGNOSIS_BUILD;

import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAdSetDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.PlanInfo;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAdSetDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.HwmResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {

  @Timed(AD_SET_DIAGNOSIS_BUILD)
  public List<HwmAdSetDiagnosis> getHwmAdSetDiagnosisList(List<DemandDiagnosis> demandDiagnosisList,
                                                          List<HwmResult> hwmResults) {
    Map<Long, Double> probMap = hwmResults.stream()
      .collect(Collectors.toMap(HwmResult::getId, HwmResult::getProbability));

    return demandDiagnosisList.stream()
      .map(demandDiagnosis -> buildHwmAdSetDiagnosis(demandDiagnosis, probMap))
      .collect(Collectors.toList());
  }

  @Timed(AD_SET_DIAGNOSIS_BUILD)
  public List<ShaleAdSetDiagnosis> getShaleAdSetDiagnosisList(List<DemandDiagnosis> demandDiagnosisList,
                                                              List<ShaleResult> shaleResults) {
    Map<Long, ShaleResult> resultMap = shaleResults.stream()
      .collect(Collectors.toMap(ShaleResult::getId, Function.identity()));

    return demandDiagnosisList.stream()
      .map(demandDiagnosis -> buildShaleAdSetDiagnosis(demandDiagnosis, resultMap))
      .collect(Collectors.toList());
  }

  public PlanInfo buildPlanInfo(GraphContext graphContext, BreakContext breakContext) {
    return PlanInfo.builder()
      .planType(graphContext.getPlanType())
      .breakTypeId(graphContext.getBreakTypeGroup().getBreakTypeIds())
      .breakType(graphContext.getBreakTypeGroup().getBreakTypes())
      .breakDuration(graphContext.getBreakDuration())
      .nextBreakIndex(breakContext.getNextBreakIndex())
      .totalBreakNumber(breakContext.getTotalBreakNumber())
      .estimatedModelBreakIndex(breakContext.getEstimatedModelBreakIndex())
      .expectedRatio(breakContext.getExpectedRatio())
      .expectedProgress(breakContext.getExpectedProgress())
      .build();
  }

  private ShaleAdSetDiagnosis buildShaleAdSetDiagnosis(DemandDiagnosis demandDiagnosis,
                                                       Map<Long, ShaleResult> shaleResultMap) {
    return ShaleAdSetDiagnosis.builder()
      .adSetId(demandDiagnosis.getAdSetId())
      .demand(demandDiagnosis.getDemand())
      .campaignId(demandDiagnosis.getCampaignId())
      .delivered(demandDiagnosis.getDelivered())
      .order(demandDiagnosis.getOrder())
      .theta(shaleResultMap.get(demandDiagnosis.getAdSetId()).getTheta())
      .sigma(shaleResultMap.get(demandDiagnosis.getAdSetId()).getSigma())
      .alpha(shaleResultMap.get(demandDiagnosis.getAdSetId()).getAlpha())
      .mean(shaleResultMap.get(demandDiagnosis.getAdSetId()).getMean())
      .std(shaleResultMap.get(demandDiagnosis.getAdSetId()).getStd())
      .reachEnabled(shaleResultMap.get(demandDiagnosis.getAdSetId()).getReachEnabled())
      .target(demandDiagnosis.getTarget())
      .adDuration(demandDiagnosis.getAdDuration())
      .build();
  }


  private HwmAdSetDiagnosis buildHwmAdSetDiagnosis(DemandDiagnosis demandDiagnosis,
                                                   Map<Long, Double> adSetProbabilityMap) {
    return HwmAdSetDiagnosis.builder()
      .adSetId(demandDiagnosis.getAdSetId())
      .demand(demandDiagnosis.getDemand())
      .campaignId(demandDiagnosis.getCampaignId())
      .delivered(demandDiagnosis.getDelivered())
      .order(demandDiagnosis.getOrder())
      .probability(adSetProbabilityMap.get(demandDiagnosis.getAdSetId()))
      .target(demandDiagnosis.getTarget())
      .adDuration(demandDiagnosis.getAdDuration())
      .build();
  }
}
