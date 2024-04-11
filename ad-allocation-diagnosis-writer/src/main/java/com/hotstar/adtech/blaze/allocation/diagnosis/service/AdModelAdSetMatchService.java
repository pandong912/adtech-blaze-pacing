package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.AD_MODEL_AD_SET;

import com.hotstar.adtech.blaze.admodel.client.index.InvertedIndex;
import com.hotstar.adtech.blaze.admodel.client.model.AdSetInfo;
import com.hotstar.adtech.blaze.admodel.client.model.AudienceTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.ContentAdSetInfo;
import com.hotstar.adtech.blaze.admodel.client.model.ImageAd;
import com.hotstar.adtech.blaze.admodel.client.model.StreamTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.VideoAd;
import com.hotstar.adtech.blaze.admodel.common.enums.CreativeType;
import com.hotstar.adtech.blaze.allocation.diagnosis.entity.AdModelData;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelAdSetMatch;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AdModelAdSetMatchSink;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdModelAdSetMatchService {
  private final AdModelAdSetMatchSink adModelAdSetMatchSink;

  @Timed(AD_MODEL_AD_SET)
  public void writeMatchAdSet(AdModelData adModelData) {
    Instant version = adModelData.getVersion();
    List<ContentAdSetInfo> goalMatches = adModelData.getLiveEntities().getInvertedIndexMap().values().stream()
      .map(InvertedIndex::getContentAdSets)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
    Map<Long, AdSetInfo> adSetInfoMap = adModelData.getAdEntities().getAdSets().stream()
      .collect(Collectors.toMap(AdSetInfo::getId, adSetInfo -> adSetInfo));
    List<AdModelAdSetMatch> collect = goalMatches.stream()
      .map(goalMatchInfo -> buildAdModelAdSe(goalMatchInfo, version, adSetInfoMap.get(goalMatchInfo.getAdSetId())))
      .collect(Collectors.toList());
    adModelAdSetMatchSink.write(collect);
  }

  private AdModelAdSetMatch buildAdModelAdSe(ContentAdSetInfo goalMatchInfo, Instant version,
                                             AdSetInfo adSetInfo) {
    return AdModelAdSetMatch.builder()
      .version(version)
      .breakDuration(goalMatchInfo.getBreakDuration())
      .adSetId(goalMatchInfo.getAdSetId())
      .siMatchId(goalMatchInfo.getContentId())
      .campaignId(goalMatchInfo.getCampaignId())
      .brandId(goalMatchInfo.getBrandId())
      .campaignStatus(goalMatchInfo.getCampaignStatus().toString())
      .campaignType(goalMatchInfo.getCampaignType().toString())
      .enabled(goalMatchInfo.isEnabled())
      .astonAds(goalMatchInfo.getImageAds().stream().map(ImageAd::getId).collect(Collectors.toList()))
      .industryId(goalMatchInfo.getIndustryId())
      .automateDelivery(goalMatchInfo.isAutomateDelivery())
      .priority(goalMatchInfo.getPriority())
      .spotAds(goalMatchInfo.getVideoAds().stream().filter(ad -> ad.getCreativeType() == CreativeType.Spot)
        .map(VideoAd::getId).collect(Collectors.toList()))
      .ssaiAds(goalMatchInfo.getVideoAds().stream().filter(ad -> ad.getCreativeType() == CreativeType.SSAI)
        .map(VideoAd::getId).collect(Collectors.toList()))
      .astonAds(goalMatchInfo.getImageAds().stream().map(ImageAd::getId).collect(Collectors.toList()))
      .breakTargetingRuleType(adSetInfo.getBreakTargetingRule() == null ? "" :
        adSetInfo.getBreakTargetingRule().getRuleType().toString())
      .breakTargetingRuleInfo(adSetInfo.getBreakTargetingRule() == null ? Collections.emptyList() :
        adSetInfo.getBreakTargetingRule().getBreakTypeIds())
      .impressionTarget(goalMatchInfo.getImpressionTarget())
      .maximiseReach(goalMatchInfo.isMaximiseReach())
      .streamTargetingRuleType(adSetInfo.getStreamTargetingRule() == null ? "" :
        adSetInfo.getStreamTargetingRule().getRuleType().toString())
      .streamTargetingRuleInfo(adSetInfo.getStreamTargetingRule() == null ? "" :
        adSetInfo.getStreamTargetingRule().getStreamTargetingRuleClauses().stream()
          .map(this::buildStreamTargetingRuleClauses).collect(Collectors.toList()).toString())
      .audienceTargetingRuleInfo(adSetInfo.getAudienceTargetingRule() == null ? "" :
        adSetInfo.getAudienceTargetingRule().getAudienceTargetingRuleClauses().stream()
          .map(this::buildAudienceTargetingRuleInfo).collect(Collectors.toList()).toString())
      .build();
  }

  private String buildAudienceTargetingRuleInfo(AudienceTargetingRuleClauseInfo audienceTargetingRuleClauseInfo) {
    return audienceTargetingRuleClauseInfo.getRuleType() + ":"
      + audienceTargetingRuleClauseInfo.getTargetingTags().toString();
  }

  private String buildStreamTargetingRuleClauses(StreamTargetingRuleClauseInfo s) {
    return s.getTenant() + "+" + s.getLanguageId() + "+" + s.getLadder() + "+" + s.getStreamType();
  }
}
