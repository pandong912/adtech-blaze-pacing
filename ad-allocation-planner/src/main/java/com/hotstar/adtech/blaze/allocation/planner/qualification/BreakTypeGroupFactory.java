package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.RuleFeasibleProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.RuleFeasible;
import com.hotstar.adtech.blaze.allocation.planner.qualification.index.TargetingEngine;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakTypeGroup;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Service;

@Service
public class BreakTypeGroupFactory {

  public List<BreakTypeGroup> getBreakTypeList(RuleFeasibleProtocol breakTargeting, List<BreakDetail> breakDetails) {
    TargetingEngine breakEngine = new TargetingEngine(RuleFeasible.of(breakTargeting));
    Map<BitSet, List<BreakResult>> group = breakDetails.stream()
      .map(breakDetail -> qualify(breakDetail, breakEngine))
      .collect(Collectors.groupingBy(BreakResult::getQualifiedAdSet));
    return group.values().stream()
      .map(results -> groupBreakType(results.stream().map(BreakResult::getBreakDetail).collect(Collectors.toList())))
      .collect(Collectors.toList());
  }

  private BreakResult qualify(BreakDetail breakDetail, TargetingEngine breakEngine) {
    String breakTypeId = String.valueOf(breakDetail.getBreakTypeId());
    BitSet result = breakEngine.targeting(breakTypeId);
    return BreakResult.of(breakDetail, result);
  }

  private BreakTypeGroup groupBreakType(List<BreakDetail> list) {
    return BreakTypeGroup.builder()
      .breakTypeIds(list.stream().map(BreakDetail::getBreakTypeId).sorted().collect(Collectors.toList()))
      .allBreakDurations(
        list.stream().map(BreakDetail::getBreakDuration).flatMap(List::stream).collect(Collectors.toSet()))
      .build();
  }

  @Value
  @Builder
  private static class BreakResult {
    BitSet qualifiedAdSet;
    BreakDetail breakDetail;

    public static BreakResult of(BreakDetail breakDetail, BitSet qualified) {
      return BreakResult.builder()
        .qualifiedAdSet(qualified)
        .breakDetail(breakDetail)
        .build();
    }
  }
}
