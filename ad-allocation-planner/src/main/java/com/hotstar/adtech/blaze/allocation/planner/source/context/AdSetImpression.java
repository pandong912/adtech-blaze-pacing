package com.hotstar.adtech.blaze.allocation.planner.source.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdSetImpression {
  private Long adSetId;
  private Long impression;
}
