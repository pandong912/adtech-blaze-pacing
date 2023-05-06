package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.MatchState;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@ToString
@Getter
@Builder
@AllArgsConstructor
public class Match {
  private String contentId;
  private Instant startTime;
  private MatchState state;
}
