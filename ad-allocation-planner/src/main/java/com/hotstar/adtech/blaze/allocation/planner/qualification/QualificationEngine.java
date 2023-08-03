package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import java.util.List;

public interface QualificationEngine {

  void qualify(List<AdSet> candidateAdSets);

}
