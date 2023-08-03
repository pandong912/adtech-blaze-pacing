package com.hotstar.adtech.blaze.allocation.planner.common.response.shale;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SupplyInfo {
  Map<String, Integer> supplyIdMap;
}
