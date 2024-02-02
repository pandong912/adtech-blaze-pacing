package com.hotstar.adtech.blaze.allocation.planner.common.request;

import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UnReachData {
  private static final String SSAI_TAG_SPLITTER = "|";
  String ssaiTag;
  PlayoutStream playoutStream;
  Map<Long, Double> unReachRatio;

  public String getKey() {
    return playoutStream.getPlayoutId() + SSAI_TAG_SPLITTER + ssaiTag;
  }
}
