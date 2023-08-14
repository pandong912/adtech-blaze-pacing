package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import java.util.List;

public class StreamAdSetQualificationEngine implements QualificationEngine {

  private final StreamTargetingRuleInspector streamTargetingRuleInspector;
  private final int supplyId;
  private final QualificationResult firstQualified;

  public StreamAdSetQualificationEngine(PlayoutStream playoutStream, int supplyId, QualificationResult firstQualified) {
    Tenant tenant = playoutStream.getTenant();
    Integer languageId = playoutStream.getLanguage().getId();
    List<Integer> platformIds = playoutStream.getPlatformIds();
    streamTargetingRuleInspector = new StreamTargetingRuleInspector(tenant, languageId, platformIds);
    this.supplyId = supplyId;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    for (AdSet candidateAdSet : candidateAdSets) {
      if (streamTargetingRuleInspector.qualify(candidateAdSet)) {
        firstQualified.set(supplyId, candidateAdSet.getDemandId());
      }
    }
  }
}
