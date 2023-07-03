package com.hotstar.adtech.blaze.adserver.ingester.entity;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdModel {
  private final List<Match> matches;
  private final Map<String, Ad> adMap;
  private final AdModelVersion adModelVersion;
}
