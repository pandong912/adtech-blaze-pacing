package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;

public class CohortAdQualificationEngine implements QualificationEngine {

  private final DurationInspector durationInspector;
  private final LanguageInspector languageInspector;
  private final int supplyId;
  private final BitSet secondQualified;
  private final BitSet firstQualified;

  public CohortAdQualificationEngine(Integer breakDuration, Integer languageId, int supplyId, BitSet firstQualified,
                                     BitSet secondQualified) {
    durationInspector = new DurationInspector(breakDuration);
    languageInspector = new LanguageInspector(languageId);
    this.supplyId = supplyId;
    this.secondQualified = secondQualified;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    int size = MemoryAlignment.getSize(candidateAdSets);
    for (AdSet candidateAdSet : candidateAdSets) {
      if (!firstQualified.get(supplyId * size + candidateAdSet.getDemandId())) {
        continue;
      }
      if (isQualified(candidateAdSet)) {
        secondQualified.set(supplyId * size + candidateAdSet.getDemandId());
      }
    }
  }

  private boolean isQualified(AdSet candidateAdSet) {
    for (Ad ssaiAd : candidateAdSet.getSsaiAds()) {
      if (durationInspector.qualify(ssaiAd) && languageInspector.qualify(ssaiAd)) {
        return true;
      }
    }
    return false;
  }
}
