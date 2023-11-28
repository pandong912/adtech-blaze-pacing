package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.AudienceTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import java.util.List;
import java.util.Map;

public class CohortAdSetQualificationEngine implements QualificationEngine {

  private final AudienceTargetingRuleInspector audienceTargetingRuleInspector;
  private final int supplyId;
  private final QualificationResult firstQualified;
  private final StreamTargetingRuleInspector streamTargetingRuleInspector;

  public CohortAdSetQualificationEngine(PlayoutStream playoutStream, String ssaiTag,
                                        Map<String, Integer> targetingTagToAttributeId, int supplyId,
                                        QualificationResult firstQualified) {
    Tenant tenant = playoutStream.getTenant();
    Integer languageId = playoutStream.getLanguage().getId();
    List<Integer> platformIds = playoutStream.getPlatformIds();
    streamTargetingRuleInspector = new StreamTargetingRuleInspector(tenant, languageId, platformIds);
    audienceTargetingRuleInspector =
      new AudienceTargetingRuleInspector(ssaiTag, targetingTagToAttributeId);
    this.supplyId = supplyId;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    for (AdSet candidateAdSet : candidateAdSets) {
      if (audienceTargetingRuleInspector.qualify(candidateAdSet)
        && streamTargetingRuleInspector.qualify(candidateAdSet)) {
        firstQualified.set(supplyId, candidateAdSet.getDemandId());
      }
    }
  }

}
