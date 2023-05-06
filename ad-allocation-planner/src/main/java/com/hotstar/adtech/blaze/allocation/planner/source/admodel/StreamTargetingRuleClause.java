package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import com.hotstar.adtech.blaze.admodel.common.enums.Platform;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamLanguage;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreamTargetingRuleClause {

  private Tenant tenant;
  private StreamLanguage streamLanguage;
  private Platform platform;
  private LanguageInfo language;
}
