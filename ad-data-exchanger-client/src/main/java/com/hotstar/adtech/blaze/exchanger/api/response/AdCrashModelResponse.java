package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.Distribution;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdCrashModelResponse {
  List<Distribution> distributions;
}
