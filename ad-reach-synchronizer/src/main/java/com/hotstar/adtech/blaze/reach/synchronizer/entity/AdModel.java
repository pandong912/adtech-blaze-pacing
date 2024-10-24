package com.hotstar.adtech.blaze.reach.synchronizer.entity;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdModel {
  List<Match> matches;
  Map<String, List<AdSet>> contentIdToAdSets;
  long versionTimestamp;
}
