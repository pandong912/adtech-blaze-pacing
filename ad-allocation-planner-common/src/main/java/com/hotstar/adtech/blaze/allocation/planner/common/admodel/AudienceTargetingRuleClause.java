package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudienceTargetingRuleClause {

  private Integer categoryId;
  private Set<String> targetingTags;
}
