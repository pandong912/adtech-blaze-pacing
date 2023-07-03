package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamQualificationEngine implements QualificationEngine<AdSet> {

  private final StreamTargetingRuleInspector streamTargetingRuleInspector;
  private final LanguageInspector languageInspector;

  public StreamQualificationEngine(PlayoutStream playoutStream) {
    Tenant tenant = playoutStream.getTenant();
    Integer languageId = playoutStream.getLanguage().getId();
    List<Integer> platformIds = playoutStream.getPlatformIds();
    streamTargetingRuleInspector = new StreamTargetingRuleInspector(tenant, languageId, platformIds);
    languageInspector = new LanguageInspector(languageId);
  }

  public List<QualifiedAdSet> qualify(List<AdSet> candidateAdSets) {
    Map<Long, AdSet> adSetMap = candidateAdSets.stream().collect(Collectors.toMap(
      AdSet::getId, Function.identity()));

    return candidateAdSets.stream()
      .filter(streamTargetingRuleInspector::qualify)
      .flatMap(adSet -> adSet.getSpotAds().stream())
      .filter(languageInspector::qualify)
      .collect(Collectors.groupingBy(Ad::getAdSetId))
      .entrySet().stream()
      .map(entry -> QualifiedAdSet.builder()
        .id(entry.getKey())
        .qualifiedAds(entry.getValue())
        .breakTargetingRule(adSetMap.get(entry.getKey()).getBreakTargetingRule())
        .build())
      .collect(Collectors.toList());
  }

}
