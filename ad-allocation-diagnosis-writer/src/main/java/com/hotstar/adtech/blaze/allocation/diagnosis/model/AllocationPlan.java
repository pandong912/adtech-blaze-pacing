package com.hotstar.adtech.blaze.allocation.diagnosis.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationPlan {
  Instant version;
  String siMatchId;
  Long planId;
  String planType;
  String breakType;
  String algorithmType;
  Integer breakDuration;
  Integer nextBreakIndex;
  Integer totalBreakNumber;
  Integer estimatedModelBreakIndex;
  Double expectedRatio;
  Double expectedProgress;
}

