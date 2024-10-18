package com.hotstar.adtech.blaze.allocation.planner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Platform {

  private final Integer id;
  private final String name;
  private final String tag;
}
