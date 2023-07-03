package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRuleClause;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamTargetingRuleInspector implements Inspector<AdSet> {

  private final Tenant tenant;
  private final Integer languageId;
  private final List<Integer> platformIds;

  @Override
  public boolean qualify(AdSet adSet) {
    StreamTargetingRule streamTargetingRule = adSet.getStreamTargetingRule();
    if (streamTargetingRule == null) {
      return true;
    }

    if (streamTargetingRule.getTenant() != tenant) {
      return false;
    }

    List<StreamTargetingRuleClause> ruleClauses = streamTargetingRule.getStreamTargetingRuleClauses();
    if (Objects.equals(streamTargetingRule.getRuleType(), RuleType.Include)) {
      return platformIds.stream().allMatch(platformId -> ruleClauses.stream()
        .anyMatch(streamTargetingRulePredictor(platformId, languageId, tenant)));
    } else {
      return platformIds.stream().noneMatch(platformId -> ruleClauses.stream()
        .anyMatch(streamTargetingRulePredictor(platformId, languageId, tenant)));
    }
  }

  private Predicate<StreamTargetingRuleClause> streamTargetingRulePredictor(Integer platformId,
                                                                            Integer languageId,
                                                                            Tenant tenant) {
    return ruleClause -> Objects.equals(ruleClause.getPlatformId(), platformId)
      && Objects.equals(ruleClause.getLanguageId(), languageId)
      && Objects.equals(ruleClause.getTenant(), tenant);
  }

}
