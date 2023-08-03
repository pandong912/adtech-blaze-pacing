package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.ShaleResultDetail;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleAllocationPlanResponse {
  String contentId;
  Integer nextBreak;
  Integer totalBreaks;
  Integer duration;
  List<ShaleResultDetail> results;
  Map<Integer, Double> cohortAllocationMap;
}
