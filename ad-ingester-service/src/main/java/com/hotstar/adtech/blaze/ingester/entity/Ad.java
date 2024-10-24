package com.hotstar.adtech.blaze.ingester.entity;

import com.hotstar.adtech.blaze.admodel.common.enums.CreativeType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Ad {
  long id;
  String creativeId;
  long adSetId;
  CreativeType creativeType;
}
