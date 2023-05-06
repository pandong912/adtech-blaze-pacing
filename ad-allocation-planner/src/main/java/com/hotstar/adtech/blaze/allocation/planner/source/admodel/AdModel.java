package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdModel {
  public static AdModel EMPTY = AdModel.builder()
    .adModelVersion(AdModelVersion.EMPTY)
    .build();

  private Languages languages;
  private Map<String, Integer> attributeId2TargetingTags;
  private Map<String, Match> matches;
  private Map<String, List<AdSet>> adSetGroup;

  private AdModelVersion adModelVersion;

}
