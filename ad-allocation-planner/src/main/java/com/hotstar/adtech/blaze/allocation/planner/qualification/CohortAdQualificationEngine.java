package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import java.util.List;

public class CohortAdQualificationEngine implements QualificationEngine {

  private final DurationInspector durationInspector;
  private final LanguageInspector languageInspector;
  private final int supplyId;
  private final QualificationResult secondQualified;
  private final QualificationResult firstQualified;

  public CohortAdQualificationEngine(Integer relaxedDuration, Integer languageId, int supplyId,
                                     QualificationResult firstQualified,
                                     QualificationResult secondQualified) {
    durationInspector = new DurationInspector(relaxedDuration);
    languageInspector = new LanguageInspector(languageId);
    this.supplyId = supplyId;
    this.secondQualified = secondQualified;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    for (AdSet candidateAdSet : candidateAdSets) {
      if (!firstQualified.get(supplyId, candidateAdSet.getDemandId())) {
        continue;
      }
      if (isQualified(candidateAdSet)) {
        secondQualified.set(supplyId, candidateAdSet.getDemandId());
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
