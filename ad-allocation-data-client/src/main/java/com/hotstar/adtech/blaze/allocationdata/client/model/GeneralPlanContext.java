package com.hotstar.adtech.blaze.allocationdata.client.model;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeneralPlanContext {
  String contentId;
  ConcurrencyData concurrencyData;
  List<AdSet> adSets;
  Map<String, Integer> attributeId2TargetingTagMap;
  List<DemandDiagnosis> demandDiagnosisList;
  List<Response> responses;
  BreakContext breakContext;
  RequestData requestData;
  List<BreakTypeGroup> breakTypeList;
  TargetingEvaluatorsProtocol targetingEvaluators;

  public boolean isEmpty() {
    return adSets.isEmpty() || (concurrencyData.getCohorts().isEmpty() && concurrencyData.getStreams().isEmpty());
  }

  // solve https://hotstar.atlassian.net/browse/LIVE-27010
  public Integer getRelaxedDuration(Integer breakTypeId, Integer duration) {
    Integer step = breakTypeList.stream()
      .filter(breakTypeGroup -> breakTypeGroup.getBreakTypeIds().contains(breakTypeId))
      .findFirst()
      .flatMap(breakTypeGroup -> breakTypeGroup.getAllBreakDurations().stream()
        .sorted()
        .filter(d -> d > duration)
        .findFirst())
      .map(d -> d - duration)
      .orElse(duration);
    return duration + step / 2;
  }
}
