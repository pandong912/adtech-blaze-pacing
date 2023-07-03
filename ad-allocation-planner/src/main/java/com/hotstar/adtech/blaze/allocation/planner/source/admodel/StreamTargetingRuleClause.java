package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreamTargetingRuleClause {

  private Tenant tenant;
  private Integer languageId;
  private Integer platformId;
}
