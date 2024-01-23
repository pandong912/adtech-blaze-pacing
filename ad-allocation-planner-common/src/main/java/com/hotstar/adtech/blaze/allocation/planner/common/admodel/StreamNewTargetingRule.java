package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamNewTargetingRule {

  private final Tenant tenant;
  private final RuleType ruleType;
  private final List<StreamNewTargetingRuleClause> streamNewTargetingRuleClauses;
}
