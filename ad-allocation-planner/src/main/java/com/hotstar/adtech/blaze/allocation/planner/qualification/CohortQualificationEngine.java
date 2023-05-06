package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.AudienceTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Language;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CohortQualificationEngine implements QualificationEngine<AdSet> {

  private final AudienceTargetingRuleInspector audienceTargetingRuleInspector;
  private final LanguageInspector languageInspector;

  public CohortQualificationEngine(ContentCohort contentCohort, Map<String, Integer> targetingTagToAttributeId,
                                   Languages languages) {
    audienceTargetingRuleInspector =
      new AudienceTargetingRuleInspector(contentCohort.getSsaiTag(), targetingTagToAttributeId);
    languageInspector = new LanguageInspector(getLanguageId(languages, contentCohort.getPlayoutStream().getLanguage()));
  }

  private int getLanguageId(Languages languages, String streamLanguage) {
    return Optional.ofNullable(streamLanguage)
      .map(languages::getByName)
      .map(Language::getId)
      .orElse(Language.getNullLanguage().getId());
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
