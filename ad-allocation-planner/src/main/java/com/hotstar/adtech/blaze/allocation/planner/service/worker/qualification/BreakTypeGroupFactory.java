package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.BreakTargetingRule;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BreakTypeGroupFactory {

  public List<BreakTypeGroup> getBreakTypeList(List<AdSet> adSets, List<BreakDetail> breakDetails) {
    Map<BreakDetail, String> breakTypeId2AdSetIdList = breakDetails.stream()
      .collect(Collectors.toMap(Function.identity(),
        breakDetail -> getQualifiedAdSet(breakDetail.getBreakTypeId(), adSets)));

    Map<String, List<BreakDetail>> adSetIds2BreakIdList =
      breakTypeId2AdSetIdList
        .entrySet()
        .stream()
        .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.collectingAndThen(Collectors.toList(),
          list -> list.stream().map(Map.Entry::getKey).collect(Collectors.toList()))));

    return adSetIds2BreakIdList.values().stream().map(this::buildBreakTypeGroup).collect(Collectors.toList());
  }

  private BreakTypeGroup buildBreakTypeGroup(List<BreakDetail> list) {
    return BreakTypeGroup.builder()
      .breakTypeIds(list.stream().map(BreakDetail::getBreakTypeId).sorted().collect(Collectors.toList()))
      .allBreakDurations(
        list.stream().map(BreakDetail::getBreakDuration).flatMap(List::stream).collect(Collectors.toSet()))
      .build();
  }

  private String getQualifiedAdSet(Integer breakTypeId, List<AdSet> adSets) {
    List<String> adSetIds = adSets.stream()
      .filter(adSet -> adSet.getBreakTargetingRule() != null)
      .filter(adSet -> qualify(adSet, breakTypeId))
      .map(AdSet::getId)
      .sorted()
      .map(String::valueOf)
      .collect(Collectors.toList());
    return String.join(",", adSetIds);
  }

  public boolean qualify(AdSet adSet, Integer breakTypeId) {
    BreakTargetingRule breakTargetingRule = adSet.getBreakTargetingRule();
    List<Integer> breakTypeIds = breakTargetingRule.getBreakTypeIds();
    if (Objects.equals(breakTargetingRule.getRuleType(), RuleType.Include)) {
      return breakTypeIds.stream().anyMatch(id -> id.equals(breakTypeId));
    } else {
      return breakTypeIds.stream().noneMatch(id -> id.equals(breakTypeId));
    }
  }

}
