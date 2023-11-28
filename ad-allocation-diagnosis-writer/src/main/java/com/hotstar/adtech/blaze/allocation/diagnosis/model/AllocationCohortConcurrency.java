package com.hotstar.adtech.blaze.allocation.diagnosis.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationCohortConcurrency {
  String siMatchId;
  Instant version;
  String contentId;
  Integer cohortId;
  String ssaiTag;
  String tenant;
  String language;
  String platforms;
  Long concurrency;
  String streamType;
  String playoutId;
}

