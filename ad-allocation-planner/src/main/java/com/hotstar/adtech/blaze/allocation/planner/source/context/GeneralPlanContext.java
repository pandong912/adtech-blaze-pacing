package com.hotstar.adtech.blaze.allocation.planner.source.context;

import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
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
  Languages languages;
}
