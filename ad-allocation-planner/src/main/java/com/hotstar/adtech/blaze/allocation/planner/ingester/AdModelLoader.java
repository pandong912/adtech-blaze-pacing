package com.hotstar.adtech.blaze.allocation.planner.ingester;

import com.hotstar.adtech.blaze.admodel.client.AdModelClient;
import com.hotstar.adtech.blaze.admodel.client.AdModelUri;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MatchEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MetaEntities;
import com.hotstar.adtech.blaze.admodel.client.model.AttributeValueInfo;
import com.hotstar.adtech.blaze.admodel.client.model.AudienceTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.AudienceTargetingRuleInfo;
import com.hotstar.adtech.blaze.admodel.client.model.BreakTargetingRuleInfo;
import com.hotstar.adtech.blaze.admodel.client.model.GoalMatchInfo;
import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import com.hotstar.adtech.blaze.admodel.client.model.SpotCreative;
import com.hotstar.adtech.blaze.admodel.client.model.SsaiCreative;
import com.hotstar.adtech.blaze.admodel.client.model.StreamTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.StreamTargetingRuleInfo;
import com.hotstar.adtech.blaze.admodel.common.enums.CampaignStatus;
import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AudienceTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AudienceTargetingRuleClause;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.BreakTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Language;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRuleClause;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdModelLoader {
  private static final double DEMAND_PACING_COEFFICIENT = 0.5d;
  private final AdModelClient adModelClient;

  public AdModel loadAdModel(AdModelVersion adModelVersion) {
    AdModelUri adModelUri = buildAdModelUri(adModelVersion);
    MetaEntities metaEntities = adModelClient.loadMetaData(adModelUri);
    LiveEntities liveEntities = adModelClient.loadLiveAdModel(adModelUri);
    MatchEntities matchEntities = adModelClient.loadMatch(adModelUri);

    List<Language> languages = metaEntities.getLanguages().stream()
      .map(this::buildLanguage)
      .collect(Collectors.toList());

    Map<String, Match> matchMap = getMatchMap(matchEntities);

    Map<String, List<AdSet>> adSetGroup = buildAdSets(liveEntities.getGoalMatches()).stream().collect(
      Collectors.groupingBy(AdSet::getContentId));

    setAdSetInternalId(adSetGroup);

    Map<String, Integer> attributeId2TargetingTags = metaEntities.getAttributeValues().stream().collect(
      Collectors.toMap(AttributeValueInfo::getSsaiTag, AttributeValueInfo::getAttributeId));

    return AdModel.builder()
      .languages(new Languages(languages))
      .matches(matchMap)
      .adSetGroup(adSetGroup)
      .adModelVersion(adModelVersion)
      .attributeId2TargetingTags(attributeId2TargetingTags)
      .build();
  }

  private void setAdSetInternalId(Map<String, List<AdSet>> adSetGroup) {
    adSetGroup.values().forEach(adSets -> {
      for (int i = 0; i < adSets.size(); i++) {
        adSets.get(i).setDemandId(i);
      }
    });
  }

  private Map<String, Match> getMatchMap(MatchEntities matchEntities) {
    return matchEntities.getMatches().stream().map(
      matchInfo -> new Match(
        matchInfo.getContentId(),
        matchInfo.getStartTime(),
        matchInfo.getState())
    ).collect(Collectors.toMap(Match::getContentId, Function.identity()));
  }

  private AdModelUri buildAdModelUri(AdModelVersion adModelVersion) {
    return AdModelUri.builder()
      .id(adModelVersion.getId())
      .path(adModelVersion.getPath())
      .version(adModelVersion.getVersion())
      .build();
  }

  private List<AdSet> buildAdSets(List<GoalMatchInfo> adSets) {
    return adSets.stream()
      .filter(goalMatchInfo -> goalMatchInfo.getCampaignStatus() != CampaignStatus.PAUSED)
      .filter(GoalMatchInfo::isEnabled)
      .map(this::buildAdSet)
      .collect(Collectors.toList());
  }

  private AdSet buildAdSet(GoalMatchInfo adSet) {
    return AdSet.builder()
      .contentId(adSet.getContentId())
      .campaignId(adSet.getCampaignId())
      .id(adSet.getGoalId())
      .impressionTarget(adSet.getImpressionTarget())
      .priority(adSet.getPriority())
      .campaignType(adSet.getCampaignType())
      .spotAds(adSet.getSpotCreatives().stream().filter(SpotCreative::isEnabled).map(this::buildSpotAd)
        .collect(Collectors.toList()))
      .ssaiAds(adSet.getSsaiCreatives().stream().filter(SsaiCreative::isEnabled).map(this::buildSsaiAd)
        .collect(Collectors.toList()))
      .audienceTargetingRule(buildAudienceTargetingRule(adSet.getAudienceTargetingRuleInfo()))
      .breakTargetingRule(buildBreakTargetingRule(adSet.getBreakTargetingRuleInfo()))
      .streamTargetingRule(buildStreamTargetingRule(adSet.getStreamTargetingRuleInfo()))
      .demandPacingCoefficient(DEMAND_PACING_COEFFICIENT)
      //todo read from goal match
      .maximizeReach(1)
      .build();
  }

  private Ad buildSsaiAd(SsaiCreative ad) {
    return Ad.builder()
      .id(ad.getId())
      .adSetId(ad.getGoalId())
      .durationMs(ad.getDurationMs())
      .enabled(ad.isEnabled())
      .languageIds(ad.getLanguageIds())
      .build();
  }

  private Ad buildSpotAd(SpotCreative ad) {
    return Ad.builder()
      .id(ad.getId())
      .adSetId(ad.getGoalId())
      .durationMs(ad.getDuration())
      .enabled(ad.isEnabled())
      .languageIds(ad.getLanguageIds())
      .build();
  }

  private Language buildLanguage(LanguageInfo languageInfo) {
    return Language.builder()
      .id(languageInfo.getId())
      .name(languageInfo.getName())
      .tag(languageInfo.getTag())
      .build();
  }

  private AudienceTargetingRule buildAudienceTargetingRule(AudienceTargetingRuleInfo audienceTargetingRuleInfo) {
    if (Objects.isNull(audienceTargetingRuleInfo)) {
      return AudienceTargetingRule.EMPTY;
    }

    Map<RuleType, List<AudienceTargetingRuleClause>> audienceTargetingRuleClauseMap =
      audienceTargetingRuleInfo.getAudienceTargetingRuleClauses().stream()
        .collect(Collectors.groupingBy(
          AudienceTargetingRuleClauseInfo::getRuleType, Collectors.mapping(
            this::buildAudienceTargetingRuleClause, Collectors.toList())));

    return AudienceTargetingRule.builder()
      .includes(audienceTargetingRuleClauseMap.getOrDefault(RuleType.Include, Collections.emptyList()))
      .excludes(audienceTargetingRuleClauseMap.getOrDefault(RuleType.Exclude, Collections.emptyList()))
      .build();
  }

  private AudienceTargetingRuleClause buildAudienceTargetingRuleClause(
    AudienceTargetingRuleClauseInfo audienceTargetingRuleClauseInfo) {
    return AudienceTargetingRuleClause.builder()
      .categoryId(audienceTargetingRuleClauseInfo.getCategoryId())
      .targetingTags(audienceTargetingRuleClauseInfo.getTargetingTags())
      .build();
  }

  private StreamTargetingRule buildStreamTargetingRule(StreamTargetingRuleInfo streamTargetingRuleInfo) {
    if (Objects.isNull(streamTargetingRuleInfo)) {
      return null;
    }

    List<StreamTargetingRuleClause> streamTargetingRuleClauses =
      streamTargetingRuleInfo.getStreamTargetingRuleClauses().stream()
        .map(this::buildStreamTargetingRuleClause)
        .collect(Collectors.toList());

    return StreamTargetingRule.builder()
      .tenant(streamTargetingRuleInfo.getTenant())
      .streamTargetingRuleClauses(streamTargetingRuleClauses)
      .ruleType(streamTargetingRuleInfo.getRuleType())
      .build();
  }

  private StreamTargetingRuleClause buildStreamTargetingRuleClause(
    StreamTargetingRuleClauseInfo streamTargetingRuleClauseInfo) {
    return StreamTargetingRuleClause.builder()
      .tenant(streamTargetingRuleClauseInfo.getTenant())
      .streamLanguage(streamTargetingRuleClauseInfo.getStreamLanguage())
      .platform(streamTargetingRuleClauseInfo.getPlatform())
      .build();
  }

  private BreakTargetingRule buildBreakTargetingRule(BreakTargetingRuleInfo breakTargetingRuleInfo) {
    if (Objects.isNull(breakTargetingRuleInfo)) {
      return null;
    }

    return BreakTargetingRule.builder()
      .breakTypeIds(breakTargetingRuleInfo.getBreakTypeIds())
      .ruleType(breakTargetingRuleInfo.getRuleType())
      .build();
  }

}
