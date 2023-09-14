package com.hotstar.adtech.blaze.allocation.diagnosis.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationAdSet {
  Instant version;
  String siMatchId;
  Long planId;
  Long adSetId;
  Integer order;
  Double demand;
  Long target;
  Long campaignId;
  Long delivered;
  Double probability;
  Double theta;
  Double alpha;
  Double sigma;
  Double mean;
}