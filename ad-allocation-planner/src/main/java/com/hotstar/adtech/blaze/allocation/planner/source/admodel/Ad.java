package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder(toBuilder = true)
@ToString
public class Ad {
  private long id;
  private long adSetId;
  private int durationMs;
  private Set<Integer> languageIds;
  private boolean enabled;
  private String aspectRatio;

}

