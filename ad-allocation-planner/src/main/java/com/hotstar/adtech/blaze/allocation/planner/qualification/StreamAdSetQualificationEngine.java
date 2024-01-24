package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamNewTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import java.util.List;

public class StreamAdSetQualificationEngine implements QualificationEngine {

  private final StreamTargetingRuleInspector streamTargetingRuleInspector;
  private final StreamNewTargetingRuleInspector streamNewTargetingRuleInspector;
  private final int supplyId;
  private final QualificationResult firstQualified;

  public StreamAdSetQualificationEngine(PlayoutStream playoutStream, int supplyId, QualificationResult firstQualified) {
    Tenant tenant = playoutStream.getTenant();
    Integer languageId = playoutStream.getLanguage().getId();
    List<Integer> platformIds = playoutStream.getPlatformIds();
    List<Ladder> ladders = playoutStream.getLadders();
    StreamType streamType = playoutStream.getStreamType();
    streamTargetingRuleInspector = new StreamTargetingRuleInspector(tenant, languageId, platformIds);
    streamNewTargetingRuleInspector = new StreamNewTargetingRuleInspector(tenant, languageId, ladders, streamType);
    this.supplyId = supplyId;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    for (AdSet candidateAdSet : candidateAdSets) {
      if (streamTargetingRuleInspector.qualify(candidateAdSet)
        && streamNewTargetingRuleInspector.qualify(candidateAdSet)) {
        firstQualified.set(supplyId, candidateAdSet.getDemandId());
      }
    }
  }
}
