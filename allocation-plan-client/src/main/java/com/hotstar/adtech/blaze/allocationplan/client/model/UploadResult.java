package com.hotstar.adtech.blaze.allocationplan.client.model;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResult {
  private List<Integer> breakTypeIds;
  private AlgorithmType algorithmType;
  private Integer nextBreakIndex;
  private Integer totalBreakNumber;
  private PlanType planType;
  private Integer duration;
  private String fileName;
  private String md5;
}
