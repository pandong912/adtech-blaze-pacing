package com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StreamConcurrencyDiagnosis {
  String contentId;
  long cohortId;
  StreamType streamType;
  Tenant tenant;
  String language;
  List<String> platforms;
  long concurrency;
}
