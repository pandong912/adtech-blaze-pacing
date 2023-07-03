package com.hotstar.adtech.blaze.allocation.planner.source.context;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GraphContext {
  BreakTypeGroup breakTypeGroup;
  int breakDuration;
  PlanType planType;
  List<Request> requests;
  List<Response> responses;

}
