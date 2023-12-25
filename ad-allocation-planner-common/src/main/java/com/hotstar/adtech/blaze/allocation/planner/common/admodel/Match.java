package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.MatchState;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class Match {
  String contentId;
  Long seasonId;
  Long gameId;
  Instant startTime;
  MatchState state;
}
