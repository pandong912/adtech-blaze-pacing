package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.ShalePlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
@Profile("!sim && !worker")
public class ShaleAndHwmModePublisher {

  private final ShalePlanContextLoader shalePlanContextLoader;
  private final TaskPublisher taskPublisher;


  @Timed(value = MetricNames.GENERATOR, extraTags = {"type", "shale-hwm"})
  public void publishPlan(Match match, AdModel adModel) {
    try {
      Instant version = Instant.now();
      Pair<Map<String, Integer>, ShalePlanContext> planContext =
        shalePlanContextLoader.getShalePlanContext(match, adModel);
      taskPublisher.uploadAndPublish(match, planContext.getRight(), planContext.getLeft(), version, AlgorithmType.HWM,
        AlgorithmType.SHALE, AlgorithmType.HWM);
    } catch (Exception e) {
      throw new ServiceException("Failed to publish shale-hwm mode task for match: " + match.getContentId(), e);
    }
  }
}
