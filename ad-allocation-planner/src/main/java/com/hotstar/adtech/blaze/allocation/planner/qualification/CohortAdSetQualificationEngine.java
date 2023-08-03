package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.AudienceTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class CohortAdSetQualificationEngine implements QualificationEngine {

  private final AudienceTargetingRuleInspector audienceTargetingRuleInspector;
  private final int supplyId;
  private final BitSet firstQualified;

  public CohortAdSetQualificationEngine(String ssaiTag, Map<String, Integer> targetingTagToAttributeId, int supplyId,
                                        BitSet firstQualified) {
    audienceTargetingRuleInspector =
      new AudienceTargetingRuleInspector(ssaiTag, targetingTagToAttributeId);
    this.supplyId = supplyId;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    int size = MemoryAlignment.getSize(candidateAdSets);
    for (AdSet candidateAdSet : candidateAdSets) {
      if (audienceTargetingRuleInspector.qualify(candidateAdSet)) {
        firstQualified.set(supplyId * size + candidateAdSet.getDemandId());
      }
    }
  }

}
