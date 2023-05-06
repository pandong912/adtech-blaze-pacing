package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.BreakTargetingRule;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BreakTargetingRuleInspector implements Inspector<QualifiedAdSet> {

  private final long breakTypeId;

  @Override
  public boolean qualify(QualifiedAdSet adSet) {
    BreakTargetingRule breakTargetingRule = adSet.getBreakTargetingRule();

    if (breakTargetingRule == null) {
      return true;
    }

    List<Long> breakTypeIds = breakTargetingRule.getBreakTypeIds();
    if (Objects.equals(breakTargetingRule.getRuleType(), RuleType.Include)) {
      return breakTypeIds.stream().anyMatch(id -> id.equals(breakTypeId));
    } else {
      return breakTypeIds.stream().noneMatch(id -> id.equals(breakTypeId));
    }
  }
}
