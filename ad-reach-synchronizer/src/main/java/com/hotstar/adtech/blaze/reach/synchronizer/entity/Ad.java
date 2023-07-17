package  com.hotstar.adtech.blaze.reach.synchronizer.entity;

import com.hotstar.adtech.blaze.admodel.common.enums.CreativeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ad {
  private long id;
  private String creativeId;
  private long adSetId;
  private CreativeType creativeType;
}
