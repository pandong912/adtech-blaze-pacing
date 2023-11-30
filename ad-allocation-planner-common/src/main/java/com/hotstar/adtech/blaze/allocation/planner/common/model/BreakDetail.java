package com.hotstar.adtech.blaze.allocation.planner.common.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BreakDetail {
  private final int breakTypeId;
  private final String breakType;
  private final List<Integer> breakDuration;
  private final Long gameId;
}
