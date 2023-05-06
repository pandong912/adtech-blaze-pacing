package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset;

import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import com.hotstar.adtech.blaze.admodel.common.enums.Platform;
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
  private final LanguageInfo language;
  private final List<Platform> platforms;

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
      return platforms.stream().allMatch(platform -> ruleClauses.stream()
        .anyMatch(streamTargetingRulePredictor(platform, language, tenant)));
    } else {
      return platforms.stream().noneMatch(platform -> ruleClauses.stream()
        .anyMatch(streamTargetingRulePredictor(platform, language, tenant)));
    }
  }

  private Predicate<StreamTargetingRuleClause> streamTargetingRulePredictor(Platform platform,
                                                                            LanguageInfo language,
                                                                            Tenant tenant) {
    return ruleClause -> Objects.equals(ruleClause.getPlatform(), platform)
      && Objects.equals(extractLanguage(ruleClause), language.getName())
      && Objects.equals(ruleClause.getTenant(), tenant);
  }

  private String extractLanguage(StreamTargetingRuleClause clause) {
    if (clause.getLanguage() != null) {
      return clause.getLanguage().getName();
    } else {
      return clause.getStreamLanguage().toString();
    }
  }

}
