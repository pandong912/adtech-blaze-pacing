package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad.DurationInspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import java.util.List;
import java.util.stream.Collectors;

public class CohortBreakQualificationEngine implements QualificationEngine<QualifiedAdSet> {

  private final DurationInspector durationInspector;

  public CohortBreakQualificationEngine(Integer breakDuration) {
    durationInspector = new DurationInspector(breakDuration);
  }

  public List<QualifiedAdSet> qualify(List<QualifiedAdSet> candidateAdSets) {
    return candidateAdSets.stream()
      .flatMap(adSet -> adSet.getQualifiedAds().stream())
      .filter(durationInspector::qualify)
      .collect(Collectors.groupingBy(Ad::getAdSetId))
      .entrySet().stream()
      .map(entry -> QualifiedAdSet.builder().id(entry.getKey()).qualifiedAds(entry.getValue()).build())
      .collect(Collectors.toList());
  }
}
