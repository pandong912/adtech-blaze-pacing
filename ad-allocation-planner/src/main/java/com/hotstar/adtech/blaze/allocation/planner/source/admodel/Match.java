package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.MatchState;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class Match {
  String contentId;
  Long seasonId;
  Instant startTime;
  MatchState state;
}
