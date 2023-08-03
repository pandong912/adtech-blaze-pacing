package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.AspectRatioInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.LanguageInspector;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.adset.BreakTargetingRuleInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;

public class StreamAdQualificationEngine implements QualificationEngine {

  private final DurationInspector durationInspector;
  private final BreakTargetingRuleInspector breakTargetingRuleInspector;
  private final LanguageInspector languageInspector;
  private final AspectRatioInspector aspectRatioInspector;
  private final int supplyId;
  private final BitSet secondQualified;
  private final BitSet firstQualified;

  public StreamAdQualificationEngine(Integer breakDuration, Integer breakTypeId, Language language, int supplyId,
                                     BitSet firstQualified, BitSet secondQualified) {
    durationInspector = new DurationInspector(breakDuration);
    breakTargetingRuleInspector = new BreakTargetingRuleInspector(breakTypeId);
    languageInspector = new LanguageInspector(language.getId());
    aspectRatioInspector = new AspectRatioInspector(language.getName());

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
      if (!breakTargetingRuleInspector.qualify(candidateAdSet)) {
        continue;
      }
      boolean qualified = candidateAdSet.getSpotAds().stream()
        .anyMatch(
          ad -> languageInspector.qualify(ad) && durationInspector.qualify(ad) && aspectRatioInspector.qualify(ad));
      if (qualified) {
        secondQualified.set(supplyId * size + candidateAdSet.getDemandId());
      }
    }
  }
}
