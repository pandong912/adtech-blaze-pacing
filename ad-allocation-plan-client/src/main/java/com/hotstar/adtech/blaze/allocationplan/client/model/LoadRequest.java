package com.hotstar.adtech.blaze.allocationplan.client.model;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadRequest {
  private String path;
  private String fileName;
  private AlgorithmType algorithmType;
}
