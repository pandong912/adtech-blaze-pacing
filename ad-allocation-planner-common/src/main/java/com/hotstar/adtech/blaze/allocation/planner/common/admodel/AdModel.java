package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Builder
@Slf4j
public class AdModel {
  public static AdModel EMPTY = AdModel.builder()
    .adModelVersion(AdModelVersion.EMPTY)
    .build();

  private Map<String, Integer> attributeId2TargetingTags;
  private Map<String, Match> matches;
  private Map<Long, Map<String, PlayoutStream>> playoutStreamGroup;
  private Map<String, PlayoutStream> globalPlayoutStreamMap;
  private Map<String, List<AdSet>> adSetGroup;

  private AdModelVersion adModelVersion;

  public Map<String, PlayoutStream> getPlayoutStreamMap(Long seasonId) {
    return playoutStreamGroup.getOrDefault(seasonId, globalPlayoutStreamMap);
  }

}
