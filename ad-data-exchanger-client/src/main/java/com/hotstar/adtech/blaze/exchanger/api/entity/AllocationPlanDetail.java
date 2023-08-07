package com.hotstar.adtech.blaze.exchanger.api.entity;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationPlanDetail {
  PlanType planType;
  Integer totalBreakNumber;
  Integer nextBreakIndex;
  List<Integer> breakTypeIds;
  Integer duration;
  String fileName;
  String md5;
  long planId;
  AlgorithmType algorithmType;
}
