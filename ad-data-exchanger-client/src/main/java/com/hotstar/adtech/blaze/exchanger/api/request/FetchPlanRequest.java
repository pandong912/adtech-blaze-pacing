package com.hotstar.adtech.blaze.exchanger.api.request;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FetchPlanRequest {
  String contentId;
  Long version;
  PlanType planType;
}
