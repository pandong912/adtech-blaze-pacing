package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Language;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamQualificationEngine implements QualificationEngine<AdSet> {

  private final StreamTargetingRuleInspector streamTargetingRuleInspector;
  private final LanguageInspector languageInspector;

  public StreamQualificationEngine(ContentStream contentStream, Languages languages) {
    PlayoutStream playoutStream = contentStream.getPlayoutStream();
    streamTargetingRuleInspector =
      new StreamTargetingRuleInspector(playoutStream.getTenant(), getLanguageInfo(languages,
          playoutStream.getLanguage()), playoutStream.getPlatforms());
    languageInspector = new LanguageInspector(getLanguageId(languages, contentStream.getPlayoutStream().getLanguage()));
  }

  private int getLanguageId(Languages languages, String streamLanguage) {
    return Optional.ofNullable(streamLanguage)
      .map(languages::getByName)
      .map(Language::getId)
      .orElse(Language.getNullLanguage().getId());
  }

  private LanguageInfo getLanguageInfo(Languages languages, String streamLanguage) {
    return Optional.ofNullable(streamLanguage)
        .map(languages::getByName)
        .map(Language::toLanguageInfo)
        .orElse(Language.getNullLanguage().toLanguageInfo());
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
