package com.hotstar.adtech.blaze.exchanger.api.entity;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AllocationPlanDetailResponse {
  PlanType planType;
  Integer totalBreakNumber;
  Integer nextBreakIndex;
  List<Long> breakTypeIds;
  Integer duration;
  String fileName;
  String md5;
  long planId;
  AlgorithmType algorithmType;
}