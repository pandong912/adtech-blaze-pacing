package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Language {

  private final Integer id;
  private final String name;
  private final String tag;
}
