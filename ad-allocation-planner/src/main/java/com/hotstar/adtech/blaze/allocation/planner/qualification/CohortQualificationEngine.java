package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.AudienceTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CohortQualificationEngine implements QualificationEngine<AdSet> {

  private final AudienceTargetingRuleInspector audienceTargetingRuleInspector;
  private final LanguageInspector languageInspector;

  public CohortQualificationEngine(String ssaiTag, Map<String, Integer> targetingTagToAttributeId,
                                   Integer languageId) {
    audienceTargetingRuleInspector =
      new AudienceTargetingRuleInspector(ssaiTag, targetingTagToAttributeId);
    languageInspector = new LanguageInspector(languageId);
  }

  public List<QualifiedAdSet> qualify(List<AdSet> candidateAdSets) {
    return candidateAdSets.stream()
      .filter(audienceTargetingRuleInspector::qualify)
      .flatMap(adSet -> adSet.getSsaiAds().stream())
      .filter(languageInspector::qualify)
      .collect(Collectors.groupingBy(Ad::getAdSetId))
      .entrySet().stream()
      .map(entry -> QualifiedAdSet.builder().id(entry.getKey()).qualifiedAds(entry.getValue()).build())
      .collect(Collectors.toList());
  }

}
