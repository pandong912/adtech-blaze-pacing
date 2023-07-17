package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.BreakId;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BreakListResponse {
  String playoutId;
  List<BreakId> breakIds;
}
