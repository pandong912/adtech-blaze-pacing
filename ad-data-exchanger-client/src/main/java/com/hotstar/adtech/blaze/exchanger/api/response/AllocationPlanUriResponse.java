package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.AllocationPlanDetail;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationPlanUriResponse {
  String path;
  Long version;
  List<AllocationPlanDetail> allocationPlanDetails;
}
