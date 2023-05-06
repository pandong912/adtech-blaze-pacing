package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudienceTargetingRule {

  public static final AudienceTargetingRule EMPTY = AudienceTargetingRule
    .builder()
    .includes(Collections.emptyList())
    .excludes(Collections.emptyList())
    .build();

  private final List<AudienceTargetingRuleClause> includes;
  private final List<AudienceTargetingRuleClause> excludes;

}
