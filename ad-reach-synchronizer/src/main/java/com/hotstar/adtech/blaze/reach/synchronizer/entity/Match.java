package  com.hotstar.adtech.blaze.reach.synchronizer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Match {
  private String siMatchId;
  private String contentId;
}
