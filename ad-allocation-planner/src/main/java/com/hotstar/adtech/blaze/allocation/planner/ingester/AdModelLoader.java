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
import com.hotstar.adtech.blaze.admodel.client.model.MatchInfo;
import com.hotstar.adtech.blaze.admodel.client.model.PlatformInfo;
import com.hotstar.adtech.blaze.admodel.client.model.StreamMappingInfo;
import com.hotstar.adtech.blaze.admodel.client.model.StreamTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.StreamTargetingRuleInfo;
import com.hotstar.adtech.blaze.admodel.client.model.VideoAd;
import com.hotstar.adtech.blaze.admodel.common.enums.CampaignStatus;
import com.hotstar.adtech.blaze.admodel.common.enums.CreativeType;
import com.hotstar.adtech.blaze.admodel.common.enums.DeliveryMode;
import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Platform;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AudienceTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AudienceTargetingRuleClause;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.BreakTargetingRule;
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

    Map<String, Match> matchMap = buildMatchMap(matchEntities);

    Map<Long, Map<String, PlayoutStream>> playoutStreamGroup =
      buildPlayoutStreamGroup(matchEntities.getStreamMappings());

    Map<String, PlayoutStream> globalPlayoutStreamMap =
      buildPlayoutStreamMap(matchEntities.getGlobalStreamMappings());

    Map<String, List<AdSet>> adSetGroup = buildAdSetGroup(liveEntities.getGoalMatches());

    setAdSetInternalId(adSetGroup);

    Map<String, Integer> attributeId2TargetingTags = metaEntities.getAttributeValues().stream().collect(
      Collectors.toMap(AttributeValueInfo::getSsaiTag, AttributeValueInfo::getAttributeId));

    return AdModel.builder()
      .matches(matchMap)
      .playoutStreamGroup(playoutStreamGroup)
      .globalPlayoutStreamMap(globalPlayoutStreamMap)
      .adSetGroup(adSetGroup)
      .adModelVersion(adModelVersion)
      .attributeId2TargetingTags(attributeId2TargetingTags)
      .build();
  }

  private AdModelUri buildAdModelUri(AdModelVersion adModelVersion) {
    return AdModelUri.builder()
      .id(adModelVersion.getId())
      .path(adModelVersion.getPath())
      .version(adModelVersion.getVersion())
      .build();
  }

  private void setAdSetInternalId(Map<String, List<AdSet>> adSetGroup) {
    adSetGroup.values().forEach(adSets -> {
      for (int i = 0; i < adSets.size(); i++) {
        adSets.get(i).setDemandId(i);
      }
    });
  }

  private Map<String, Match> buildMatchMap(MatchEntities matchEntities) {
    return matchEntities.getMatches().stream().map(this::buildMatch)
      .collect(Collectors.toMap(Match::getContentId, Function.identity()));
  }

  private Match buildMatch(MatchInfo matchInfo) {
    return Match.builder()
      .contentId(matchInfo.getContentId())
      .seasonId(matchInfo.getSeasonId())
      .startTime(matchInfo.getStartTime())
      .state(matchInfo.getState())
      .build();
  }

  private Map<Long, Map<String, PlayoutStream>> buildPlayoutStreamGroup(List<StreamMappingInfo> streamMappings) {
    return streamMappings.stream()
      .collect(Collectors.groupingBy(StreamMappingInfo::getSeasonId,
        Collectors.collectingAndThen(Collectors.toList(), this::buildPlayoutStreamMap)));
  }

  private Map<String, PlayoutStream> buildPlayoutStreamMap(List<StreamMappingInfo> streamMappings) {
    return streamMappings.stream()
      .map(this::buildPlayoutStream)
      .collect(Collectors.toMap(PlayoutStream::getPlayoutId, Function.identity()));
  }

  private PlayoutStream buildPlayoutStream(StreamMappingInfo streamMappingInfo) {
    return PlayoutStream.builder()
      .playoutId(streamMappingInfo.getPlayoutId())
      .streamType(streamMappingInfo.getStreamType())
      .tenant(streamMappingInfo.getTenant())
      .language(buildLanguage(streamMappingInfo.getLanguage()))
      .platforms(streamMappingInfo.getPlatforms().stream().map(this::buildPlatform).collect(
        Collectors.toList()))
      .build();
  }

  private Language buildLanguage(LanguageInfo languageInfo) {
    return Language.builder()
      .id(languageInfo.getId())
      .name(languageInfo.getName())
      .tag(languageInfo.getTag())
      .build();
  }

  private Platform buildPlatform(PlatformInfo platformInfo) {
    return Platform.builder()
      .id(platformInfo.getId())
      .name(platformInfo.getName())
      .tag(platformInfo.getTag())
      .build();
  }

  private Map<String, List<AdSet>> buildAdSetGroup(List<GoalMatchInfo> adSets) {
    return adSets.stream()
      .filter(goalMatchInfo -> goalMatchInfo.getCampaignStatus() != CampaignStatus.PAUSED)
      .filter(GoalMatchInfo::isEnabled)
      .filter(goalMatchInfo -> goalMatchInfo.getDeliveryMode() == DeliveryMode.SSAI
        || goalMatchInfo.getDeliveryMode() == DeliveryMode.SSAI_SPOT)
      .map(this::buildAdSet)
      // make reach-enabled adSets at the top of the list, This is to reduce the size of the reach storage array.
      .sorted((a, b) -> b.getMaximizeReach() - a.getMaximizeReach())
      .collect(Collectors.groupingBy(AdSet::getContentId));
  }

  private AdSet buildAdSet(GoalMatchInfo adSet) {
    return AdSet.builder()
      .contentId(adSet.getContentId())
      .campaignId(adSet.getCampaignId())
      .id(adSet.getAdSetId())
      .impressionTarget(adSet.getImpressionTarget())
      .priority(adSet.getPriority())
      .campaignType(adSet.getCampaignType())
      .spotAds(adSet.getVideoAds().stream().filter(VideoAd::isEnabled)
        .filter(videoAd -> videoAd.getCreativeType() == CreativeType.Spot).map(this::buildAd)
        .collect(Collectors.toList()))
      .ssaiAds(adSet.getVideoAds().stream().filter(VideoAd::isEnabled)
        .filter(videoAd -> videoAd.getCreativeType() == CreativeType.SSAI).map(this::buildAd)
        .collect(Collectors.toList()))
      .audienceTargetingRule(buildAudienceTargetingRule(adSet.getAudienceTargetingRuleInfo()))
      .breakTargetingRule(buildBreakTargetingRule(adSet.getBreakTargetingRuleInfo()))
      .streamTargetingRule(buildStreamTargetingRule(adSet.getStreamTargetingRuleInfo()))
      .demandPacingCoefficient(DEMAND_PACING_COEFFICIENT)
      .maximizeReach(adSet.isMaximiseReach() ? 1 : 0)
      .build();
  }

  private Ad buildAd(VideoAd ad) {
    return Ad.builder()
      .id(ad.getId())
      .adSetId(ad.getAdSetId())
      .durationMs(ad.getDurationMs())
      .enabled(ad.isEnabled())
      .languageIds(ad.getLanguageIds())
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
      .languageId(streamTargetingRuleClauseInfo.getLanguageId())
      .platformId(streamTargetingRuleClauseInfo.getPlatformId())
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
