package com.hotstar.adtech.blaze.allocationdata.client.model;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    return adSets.isEmpty();
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

  public Map<Integer, AdSetRemainImpr> buildRemainDeliveryMap() {
    Map<Integer, Long> index2Response = responses.stream()
      .collect(Collectors.toMap(Response::getDemandId, Response::getRemainDelivery));

    return adSets.stream()
      .map(adSet -> new AdSetRemainImpr(adSet,
        index2Response.computeIfAbsent(adSet.getDemandId(), id -> Long.MAX_VALUE)))
      .collect(Collectors.toMap(a -> a.adSet().getDemandId(), Function.identity()));
  }
}
