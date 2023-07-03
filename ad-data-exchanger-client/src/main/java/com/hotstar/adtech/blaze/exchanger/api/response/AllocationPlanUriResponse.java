package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.AllocationPlanDetailResponse;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AllocationPlanUriResponse {
  String path;
  Long version;
  List<AllocationPlanDetailResponse> allocationPlanDetailResponses;
}
