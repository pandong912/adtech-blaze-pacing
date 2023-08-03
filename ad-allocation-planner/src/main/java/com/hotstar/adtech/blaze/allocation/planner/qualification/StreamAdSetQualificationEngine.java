package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.StreamTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;

public class StreamAdSetQualificationEngine implements QualificationEngine {

  private final StreamTargetingRuleInspector streamTargetingRuleInspector;
  private final int supplyId;
  private final BitSet firstQualified;

  public StreamAdSetQualificationEngine(PlayoutStream playoutStream, int supplyId, BitSet firstQualified) {
    Tenant tenant = playoutStream.getTenant();
    Integer languageId = playoutStream.getLanguage().getId();
    List<Integer> platformIds = playoutStream.getPlatformIds();
    streamTargetingRuleInspector = new StreamTargetingRuleInspector(tenant, languageId, platformIds);
    this.supplyId = supplyId;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    int size = MemoryAlignment.getSize(candidateAdSets);
    for (AdSet candidateAdSet : candidateAdSets) {
      if (streamTargetingRuleInspector.qualify(candidateAdSet)) {
        firstQualified.set(supplyId * size + candidateAdSet.getDemandId());
      }
    }
  }
}
