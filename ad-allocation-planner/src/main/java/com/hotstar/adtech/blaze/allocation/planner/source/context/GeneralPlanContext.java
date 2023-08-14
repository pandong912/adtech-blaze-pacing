package com.hotstar.adtech.blaze.allocation.planner.source.context;

import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.RequestData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeneralPlanContext {
  String contentId;
  ConcurrencyData concurrencyData;
  List<AdSet> adSets;
  Map<String, Integer> attributeId2TargetingTagMap;
  List<DemandDiagnosis> demandDiagnosisList;
  List<Response> responses;
  BreakContext breakContext;
  List<BreakDetail> breakDetails;
  RequestData requestData;

  public boolean isEmpty() {
    return adSets.isEmpty() || (concurrencyData.getCohorts().isEmpty() && concurrencyData.getStreams().isEmpty());
  }
}
