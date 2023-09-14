package com.hotstar.adtech.blaze.allocation.diagnosis.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class AdModelAdSetMatch {
  Instant version;
  String siMatchId;
  Long campaignId;
  String campaignType;
  Long adSetId;
  Integer industryId;
  Long brandId;
  String campaignStatus;
  Long impressionTarget;
  Integer priority;
  Boolean maximiseReach;
  Boolean enabled;
  Long breakDuration;
  Boolean automateDelivery;
  List<Long> ssaiAds;
  List<Long> spotAds;
  List<Long> astonAds;
  String audienceTargetingRuleInfo;
  String streamTargetingRuleInfo;
  String streamTargetingRuleType;
  List<Integer> breakTargetingRuleInfo;
  String breakTargetingRuleType;
}