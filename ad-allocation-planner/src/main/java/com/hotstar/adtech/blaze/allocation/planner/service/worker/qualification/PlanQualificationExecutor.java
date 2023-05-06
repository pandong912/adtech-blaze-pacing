package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import java.util.List;

public interface PlanQualificationExecutor {
  List<GraphContext> executeQualify(GeneralPlanContext generalPlanContext);
}
