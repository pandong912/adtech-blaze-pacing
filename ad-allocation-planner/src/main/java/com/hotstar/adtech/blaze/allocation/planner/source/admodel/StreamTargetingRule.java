package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamTargetingRule {

  private Tenant tenant;
  private RuleType ruleType;
  private List<StreamTargetingRuleClause> streamTargetingRuleClauses;
}
