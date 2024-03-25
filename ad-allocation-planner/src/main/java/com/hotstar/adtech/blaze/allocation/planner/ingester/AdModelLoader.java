package com.hotstar.adtech.blaze.allocation.planner.ingester;

import com.hotstar.adtech.blaze.admodel.client.AdModelClient;
import com.hotstar.adtech.blaze.admodel.client.AdModelUri;
import com.hotstar.adtech.blaze.admodel.client.entity.ContentInvertedIndex;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MatchEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.MetaEntities;
import com.hotstar.adtech.blaze.admodel.client.entity.SsaiSpotInvertedIndexEntity;
import com.hotstar.adtech.blaze.admodel.client.index.InvertedIndex;
import com.hotstar.adtech.blaze.admodel.client.index.TargetingFeasible;
import com.hotstar.adtech.blaze.admodel.client.model.AttributeValueInfo;
import com.hotstar.adtech.blaze.admodel.client.model.BreakTypeInfo;
import com.hotstar.adtech.blaze.admodel.client.model.GoalMatchInfo;
import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import com.hotstar.adtech.blaze.admodel.client.model.MatchInfo;
import com.hotstar.adtech.blaze.admodel.client.model.PlatformInfo;
import com.hotstar.adtech.blaze.admodel.client.model.StreamMappingInfo;
import com.hotstar.adtech.blaze.admodel.client.model.VideoAd;
import com.hotstar.adtech.blaze.admodel.common.enums.CampaignStatus;
import com.hotstar.adtech.blaze.admodel.common.enums.CreativeType;
import com.hotstar.adtech.blaze.admodel.common.enums.DeliveryMode;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleFeasibleProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleInvertedIndexProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Platform;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdModelLoader {
  private static final double DEMAND_PACING_COEFFICIENT = 0.5d;
  private static final String SPOT_BREAK = "Spot";
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
    assignReachIndex(adSetGroup);

    Map<Long, List<BreakDetail>> breakTypes = metaEntities.getBreakTypes().stream()
      .filter(breakTypeInfo -> SPOT_BREAK.equals(breakTypeInfo.getType()))
      .map(this::buildBreakDetail)
      .collect(Collectors.groupingBy(BreakDetail::getGameId));

    Map<String, Integer> attributeId2TargetingTags = metaEntities.getAttributeValues().stream().collect(
      Collectors.toMap(AttributeValueInfo::getSsaiTag, AttributeValueInfo::getAttributeId));

    Map<String, ContentInvertedIndex> indexMap = liveEntities.getContentInvertedIndexMap();
    Map<String, TargetingEvaluatorsProtocol> targetingEvaluatorsMap = buildTargetingEvaluators(indexMap, adSetGroup);

    return AdModel.builder()
      .matches(matchMap)
      .playoutStreamGroup(playoutStreamGroup)
      .globalPlayoutStreamMap(globalPlayoutStreamMap)
      .adSetGroup(adSetGroup)
      .adModelVersion(adModelVersion)
      .attributeId2TargetingTags(attributeId2TargetingTags)
      .breakDetailGroup(breakTypes)
      .targetingEvaluatorsMap(targetingEvaluatorsMap)
      .build();
  }

  public static Map<String, TargetingEvaluatorsProtocol> buildTargetingEvaluators(
    Map<String, ContentInvertedIndex> contentIndexMap, Map<String, List<AdSet>> adSetGroup) {
    return contentIndexMap.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey,
        e -> buildTargetingEvaluatorsProtocol(e.getValue(),
          adSetGroup.getOrDefault(e.getKey(), Collections.emptyList()))));
  }

  private static TargetingEvaluatorsProtocol buildTargetingEvaluatorsProtocol(ContentInvertedIndex contentIndex,
                                                                              List<AdSet> adSet) {
    SsaiSpotInvertedIndexEntity index = contentIndex.getSsaiSpotInvertedIndexEntity();
    Map<Integer, RuleFeasibleProtocol> audience = index.getAudienceTargetingFeasible().entrySet()
      .stream()
      .collect(
        Collectors.toMap(Map.Entry::getKey, entry -> buildRuleFeasibleProtocol(entry.getValue(), index.getSize())));
    int size = index.getSize();
    BitSet activeAdSet = new BitSet(size);
    adSet.stream().map(AdSet::getDemandId).forEach(activeAdSet::set);
    return TargetingEvaluatorsProtocol.builder()
      .breakTargeting(buildRuleFeasibleProtocol(index.getBreakTargetingFeasible(), size))
      .stream(buildRuleFeasibleProtocol(index.getStreamTargetingFeasible(), size))
      .streamNew(buildRuleFeasibleProtocol(index.getStreamNewTargetingFeasible(), size))
      .audience(audience)
      .duration(buildRuleFeasibleProtocol(index.getDurationFeasible(), size))
      .aspectRatio(buildRuleFeasibleProtocol(index.getAspectRatioFeasible(), size))
      .language(buildRuleFeasibleProtocol(index.getLanguageFeasible(), size))
      .durationSet(index.getDurationSet())
      .activeAdSet(activeAdSet.toLongArray())
      .adSetSize(size)
      .build();
  }

  private static RuleFeasibleProtocol buildRuleFeasibleProtocol(TargetingFeasible targetingFeasible, int size) {
    Map<String, RuleInvertedIndexProtocol> ruleInvertedIndexProtocol = targetingFeasible.getInvertedIndex().entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> buildIndex(entry.getValue())));
    return RuleFeasibleProtocol.builder()
      .invertedIndex(ruleInvertedIndexProtocol)
      .size(size)
      .ignoreInclude(targetingFeasible.getIgnoreInclude())
      .build();
  }

  private static RuleInvertedIndexProtocol buildIndex(InvertedIndex entry) {
    return RuleInvertedIndexProtocol.builder()
      .include(entry.getInclude())
      .exclude(entry.getExclude())
      .tag(entry.getTag())
      .build();
  }

  private void assignReachIndex(Map<String, List<AdSet>> adSetGroup) {
    for (List<AdSet> adSets : adSetGroup.values()) {
      AtomicInteger index = new AtomicInteger();
      adSets.stream()
        .filter(adSet -> adSet.getMaximizeReach() == 1)
        .forEach(adSet -> adSet.setReachIndex(index.getAndIncrement()));
    }
  }


  private BreakDetail buildBreakDetail(BreakTypeInfo breakTypeInfo) {
    List<Integer> durationList = new ArrayList<>();
    int duration = breakTypeInfo.getLowerBound();
    while (duration < breakTypeInfo.getUpperBound()) {
      durationList.add(duration);
      duration += breakTypeInfo.getStep();
    }
    durationList.add(breakTypeInfo.getUpperBound());
    return BreakDetail.builder()
      .breakTypeId(breakTypeInfo.getId())
      .breakType(breakTypeInfo.getName())
      .breakDuration(durationList)
      .gameId(breakTypeInfo.getGameId())
      .build();
  }

  private AdModelUri buildAdModelUri(AdModelVersion adModelVersion) {
    return AdModelUri.builder()
      .id(adModelVersion.getId())
      .path(adModelVersion.getPath())
      .version(adModelVersion.getVersion())
      .build();
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
      .gameId(matchInfo.getGameId())
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
      .ladders(streamMappingInfo.getLadders())
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
      .filter(Objects::nonNull)
      .collect(Collectors.groupingBy(AdSet::getContentId));
  }

  private AdSet buildAdSet(GoalMatchInfo adSet) {
    List<Ad> spotAds = adSet.getVideoAds().stream()
      .filter(VideoAd::isEnabled)
      .filter(videoAd -> videoAd.getCreativeType() == CreativeType.Spot)
      .map(this::buildAd)
      .collect(Collectors.toList());
    List<Ad> ssaiAds = adSet.getVideoAds().stream()
      .filter(VideoAd::isEnabled)
      .filter(videoAd -> videoAd.getCreativeType() == CreativeType.SSAI)
      .map(this::buildAd)
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(spotAds) && CollectionUtils.isEmpty(ssaiAds)) {
      return null;
    }
    return AdSet.builder()
      .contentId(adSet.getContentId())
      .campaignId(adSet.getCampaignId())
      .id(adSet.getAdSetId())
      .impressionTarget(adSet.getImpressionTarget())
      .priority(adSet.getPriority())
      .campaignType(adSet.getCampaignType())
      .spotAds(spotAds)
      .ssaiAds(ssaiAds)
      .demandPacingCoefficient(DEMAND_PACING_COEFFICIENT)
      .maximizeReach(adSet.isMaximiseReach() ? 1 : 0)
      .demandId(adSet.getIndex())
      .build();
  }

  private Ad buildAd(VideoAd ad) {
    return Ad.builder()
      .id(ad.getId())
      .adSetId(ad.getAdSetId())
      .durationMs(ad.getDurationMs())
      .enabled(ad.isEnabled())
      .languageIds(ad.getLanguageIds())
      .aspectRatio(ad.getAspectRatio())
      .build();
  }
}
