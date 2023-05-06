package com.hotstar.adtech.blaze.allocation.planner.service.manager.loader;

import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor
public class ShalePlanContextLoader {
  private final GeneralPlanContextLoader generalPlanContextLoader;
  private final DataExchangerService dataExchangerService;

  public ShalePlanContext getShalePlanContext(Match match, AdModel adModel) {
    GeneralPlanContext generalPlanContext = generalPlanContextLoader.getGeneralPlanContext(match, adModel);

    return ShalePlanContext.builder()
      .generalPlanContext(generalPlanContext)
      .penalty(ShaleConstant.PENALTY)
      .build();
  }

}
