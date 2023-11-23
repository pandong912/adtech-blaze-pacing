package com.hotstar.adtech.blaze.allocationdata.client.model;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BreakTypeGroup {
  List<Integer> breakTypeIds;
  Set<Integer> allBreakDurations;
}
