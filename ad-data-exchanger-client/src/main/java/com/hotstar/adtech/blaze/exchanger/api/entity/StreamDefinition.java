package com.hotstar.adtech.blaze.exchanger.api.entity;

import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StreamDefinition {
  String playoutId;
  List<Ladder> ladders;
  StreamType streamType;
  Tenant tenant;
  LanguageEntity language;
  List<PlatformEntity> platforms;
}
