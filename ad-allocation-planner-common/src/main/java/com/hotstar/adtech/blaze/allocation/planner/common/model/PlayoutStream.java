package com.hotstar.adtech.blaze.allocation.planner.common.model;

import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
public class PlayoutStream {

  private static final String SPLITTER = "-";
  private static final String PLATFORM_SPLITTER = "+";
  Tenant tenant;
  Language language;
  List<Ladder> ladders;
  String playoutId;
  StreamType streamType;

  @Builder
  public PlayoutStream(String playoutId, StreamType streamType, Tenant tenant, Language language,
                       List<Ladder> ladders) {
    this.playoutId = playoutId;
    this.streamType = streamType;
    this.tenant = tenant;
    this.language = language;
    this.ladders = ladders;
  }
}
