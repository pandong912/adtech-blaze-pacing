package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Request {
  int concurrencyId;
  long concurrency;
  List<QualifiedAdSet> qualifiedAdSets;
}
