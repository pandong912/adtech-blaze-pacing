package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.AspectRatioInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.BreakTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import java.util.List;

public class StreamAdQualificationEngine implements QualificationEngine {

  private final DurationInspector durationInspector;
  private final BreakTargetingRuleInspector breakTargetingRuleInspector;
  private final LanguageInspector languageInspector;
  private final AspectRatioInspector aspectRatioInspector;
  private final int supplyId;
  private final QualificationResult secondQualified;
  private final QualificationResult firstQualified;

  public StreamAdQualificationEngine(Integer relaxedDuration, Integer breakTypeId, Language language, int supplyId,
                                     QualificationResult firstQualified, QualificationResult secondQualified) {
    durationInspector = new DurationInspector(relaxedDuration);
    breakTargetingRuleInspector = new BreakTargetingRuleInspector(breakTypeId);
    languageInspector = new LanguageInspector(language.getId());
    aspectRatioInspector = new AspectRatioInspector(language.getName());

    this.supplyId = supplyId;
    this.secondQualified = secondQualified;
    this.firstQualified = firstQualified;
  }

  public void qualify(List<AdSet> candidateAdSets) {
    for (AdSet candidateAdSet : candidateAdSets) {
      if (!firstQualified.get(supplyId, candidateAdSet.getDemandId())) {
        continue;
      }
      if (!breakTargetingRuleInspector.qualify(candidateAdSet)) {
        continue;
      }
      boolean qualified = candidateAdSet.getSpotAds().stream()
        .anyMatch(
          ad -> languageInspector.qualify(ad) && durationInspector.qualify(ad) && aspectRatioInspector.qualify(ad));
      if (qualified) {
        secondQualified.set(supplyId, candidateAdSet.getDemandId());
      }
    }
  }
}
