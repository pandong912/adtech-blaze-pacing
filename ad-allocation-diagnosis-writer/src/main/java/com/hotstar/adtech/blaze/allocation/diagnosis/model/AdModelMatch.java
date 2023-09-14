package com.hotstar.adtech.blaze.allocation.diagnosis.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdModelMatch {
  Instant version;
  Long id;
  String name;
  String siMatchId;
  String contentId;
  Integer languageId;
  Long tournamentId;
  Long seasonId;
  Instant startTime;
  String status;
  Instant matchVersion;
}
