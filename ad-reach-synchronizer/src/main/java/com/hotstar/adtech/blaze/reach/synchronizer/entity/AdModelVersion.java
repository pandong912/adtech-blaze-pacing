package  com.hotstar.adtech.blaze.reach.synchronizer.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdModelVersion {
  private final Long version;
  private final String adModelMd5;
  private final String liveMatchMd5;
}
