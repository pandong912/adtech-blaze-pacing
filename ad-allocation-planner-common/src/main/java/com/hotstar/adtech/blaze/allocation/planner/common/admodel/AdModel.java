package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private Map<Long, List<BreakDetail>> breakDetailGroup;
  private Map<String, TargetingEvaluatorsProtocol> targetingEvaluatorsMap;

  private AdModelVersion adModelVersion;

  public Map<String, PlayoutStream> getPlayoutStreamMap(Long seasonId) {
    return playoutStreamGroup.getOrDefault(seasonId, globalPlayoutStreamMap);
  }

  public List<BreakDetail> getBreakDetails(String contentId) {
    return Optional.ofNullable(matches.get(contentId))
      .map(Match::getGameId)
      .map(breakDetailGroup::get)
      .orElse(Collections.emptyList());
  }
}
