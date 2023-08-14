package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.AudienceTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import java.util.List;
import java.util.Map;

public class CohortAdSetQualificationEngine implements QualificationEngine {

  private final AudienceTargetingRuleInspector audienceTargetingRuleInspector;
  private final int supplyId;
  private final QualificationResult firstQualified;

  public CohortAdSetQualificationEngine(String ssaiTag, Map<String, Integer> targetingTagToAttributeId, int supplyId,
                                        QualificationResult firstQualified) {
    audienceTargetingRuleInspector =
      new AudienceTargetingRuleInspector(ssaiTag, targetingTagToAttributeId);
    this.supplyId = supplyId;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    for (AdSet candidateAdSet : candidateAdSets) {
      if (audienceTargetingRuleInspector.qualify(candidateAdSet)) {
        firstQualified.set(supplyId, candidateAdSet.getDemandId());
      }
    }
  }

}
