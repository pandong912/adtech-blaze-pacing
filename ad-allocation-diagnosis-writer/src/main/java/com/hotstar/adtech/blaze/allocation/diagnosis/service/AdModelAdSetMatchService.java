package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.AD_MODEL_AD_SET;

import com.hotstar.adtech.blaze.admodel.client.model.AudienceTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.GoalMatchInfo;
import com.hotstar.adtech.blaze.admodel.client.model.ImageAd;
import com.hotstar.adtech.blaze.admodel.client.model.StreamTargetingRuleClauseInfo;
import com.hotstar.adtech.blaze.admodel.client.model.VideoAd;
import com.hotstar.adtech.blaze.admodel.common.enums.CreativeType;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AdModelAdSetMatch;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AdModelAdSetMatchSink;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
  public void writeMatchAdSet(List<GoalMatchInfo> goalMatches, Instant version) {
    List<AdModelAdSetMatch> collect = goalMatches.stream()
      .map(goalMatchInfo ->
        AdModelAdSetMatch.builder()
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
          .breakTargetingRuleType(goalMatchInfo.getBreakTargetingRuleInfo() == null ? "" :
            goalMatchInfo.getBreakTargetingRuleInfo().getRuleType().toString())
          .breakTargetingRuleInfo(goalMatchInfo.getBreakTargetingRuleInfo() == null ? Collections.emptyList() :
            goalMatchInfo.getBreakTargetingRuleInfo().getBreakTypeIds())
          .impressionTarget(goalMatchInfo.getImpressionTarget())
          .maximiseReach(goalMatchInfo.isMaximiseReach())
          .streamTargetingRuleType(goalMatchInfo.getStreamTargetingRuleInfo() == null ? "" :
            goalMatchInfo.getStreamTargetingRuleInfo().getRuleType().toString())
          .streamTargetingRuleInfo(goalMatchInfo.getStreamTargetingRuleInfo() == null ? "" :
            goalMatchInfo.getStreamTargetingRuleInfo().getStreamTargetingRuleClauses().stream()
              .map(this::buildStreamTargetingRuleClauses).collect(Collectors.toList()).toString())
          .audienceTargetingRuleInfo(goalMatchInfo.getAudienceTargetingRuleInfo() == null ? "" :
            goalMatchInfo.getAudienceTargetingRuleInfo().getAudienceTargetingRuleClauses().stream()
              .map(this::buildAudienceTargetingRuleInfo).collect(Collectors.toList()).toString())
          .build())
      .collect(Collectors.toList());
    adModelAdSetMatchSink.write(collect);
  }

  private String buildAudienceTargetingRuleInfo(AudienceTargetingRuleClauseInfo audienceTargetingRuleClauseInfo) {
    return audienceTargetingRuleClauseInfo.getRuleType() + ":"
      + audienceTargetingRuleClauseInfo.getTargetingTags().toString();
  }

  private String buildStreamTargetingRuleClauses(StreamTargetingRuleClauseInfo s) {
    return s.getTenant() + "+" + s.getLanguageId() + "+" + s.getPlatformId();
  }
}
