package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AudienceTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AudienceTargetingRuleClause;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

public class AudienceTargetingRuleInspector implements Inspector<AdSet> {

  public static final String SSAI_TAG_PREFIX = "SSAI::";
  public static final int SSAI_TAG_PREFIX_LENGTH = SSAI_TAG_PREFIX.length();

  private final Map<Integer, Set<String>> attributeId2TargetingTags = new HashMap<>();

  public AudienceTargetingRuleInspector(String ssaiTag, Map<String, Integer> targetingTagToAttributeId) {
    String[] targetingTags = ssaiTag.substring(SSAI_TAG_PREFIX_LENGTH).split(":");
    for (String targetingTag : targetingTags) {
      Integer attributeId = targetingTagToAttributeId.get(targetingTag);
      if (attributeId == null) {
        continue; // ignore unrecognized tags
      }
      attributeId2TargetingTags.computeIfAbsent(attributeId, id -> new HashSet<>()).add(targetingTag);
    }
  }

  @Override
  public boolean qualify(AdSet adSet) {
    AudienceTargetingRule audienceTargetingRule = adSet.getAudienceTargetingRule();

    for (AudienceTargetingRuleClause audienceTargetingRuleClause : audienceTargetingRule.getExcludes()) {
      Set<String> tags = attributeId2TargetingTags.get(audienceTargetingRuleClause.getCategoryId());
      // TODO: 2023/3/6 add comments + method definition
      if (tags == null) {
        return false;
      }
      if (CollectionUtils.containsAny(audienceTargetingRuleClause.getTargetingTags(), tags)) {
        return false;
      }
    }

    for (AudienceTargetingRuleClause audienceTargetingRuleClause : audienceTargetingRule.getIncludes()) {
      Set<String> tags = attributeId2TargetingTags.get(audienceTargetingRuleClause.getCategoryId());
      if (tags == null) {
        return false;
      }
      if (!CollectionUtils.containsAll(audienceTargetingRuleClause.getTargetingTags(), tags)) {
        return false;
      }
    }

    return true;
  }
}
