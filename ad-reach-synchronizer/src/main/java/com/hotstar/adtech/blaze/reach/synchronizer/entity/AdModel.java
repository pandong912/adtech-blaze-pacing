package com.hotstar.adtech.blaze.reach.synchronizer.entity;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AdModel {
  List<Match> matches;
  Map<String, Ad> adMap;
  Map<String, List<AdSet>> adSetGroup;
  AdModelVersion adModelVersion;
}
