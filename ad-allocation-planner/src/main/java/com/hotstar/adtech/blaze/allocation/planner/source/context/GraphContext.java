package com.hotstar.adtech.blaze.allocation.planner.source.context;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import com.hotstar.adtech.blaze.allocationdata.client.model.Request;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GraphContext {
  int breakDuration;
  PlanType planType;
  List<Request> requests;
  List<Response> responses;
  List<Integer> breakTypeIds;
  QualificationResult edges;
}
