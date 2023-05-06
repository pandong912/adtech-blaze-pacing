package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BreakTargetingRule {

  private RuleType ruleType;
  private List<Long> breakTypeIds;
}
