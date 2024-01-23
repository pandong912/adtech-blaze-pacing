package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset;

import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.StreamNewTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.StreamNewTargetingRuleClause;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamNewTargetingRuleInspector implements Inspector<AdSet> {

  private final Tenant tenant;
  private final Integer languageId;
  private final List<Ladder> ladders;
  private final StreamType streamType;

  @Override
  public boolean qualify(AdSet adSet) {
    StreamNewTargetingRule streamNewTargetingRule = adSet.getStreamNewTargetingRule();

    if (streamNewTargetingRule == null) {
      return true;
    }

    if (!Objects.equals(streamNewTargetingRule.getTenant(), tenant)) {
      return false;
    }

    List<StreamNewTargetingRuleClause> ruleClauses = streamNewTargetingRule.getStreamNewTargetingRuleClauses();
    if (Objects.equals(streamNewTargetingRule.getRuleType(), RuleType.Include)) {
      return ladders.stream().allMatch(ladder -> ruleClauses.stream()
        .anyMatch(streamNewTargetingRulePredictor(ladder, languageId, tenant, streamType)));
    } else {
      return ladders.stream().noneMatch(ladder -> ruleClauses.stream()
        .anyMatch(streamNewTargetingRulePredictor(ladder, languageId, tenant, streamType)));
    }
  }

  private Predicate<StreamNewTargetingRuleClause> streamNewTargetingRulePredictor(Ladder ladder,
                                                                                  Integer languageId,
                                                                                  Tenant tenant,
                                                                                  StreamType streamType) {
    return ruleClause -> Objects.equals(ruleClause.getLadder(), ladder)
      && Objects.equals(ruleClause.getLanguageId(), languageId)
      && Objects.equals(ruleClause.getTenant(), tenant)
      && Objects.equals(ruleClause.getStreamType(), streamType);
  }

}
