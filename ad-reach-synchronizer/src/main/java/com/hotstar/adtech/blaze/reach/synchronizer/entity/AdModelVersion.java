package  com.hotstar.adtech.blaze.reach.synchronizer.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdModelVersion {
  Long version;
  String adModelMd5;
  String liveMatchMd5;
}
