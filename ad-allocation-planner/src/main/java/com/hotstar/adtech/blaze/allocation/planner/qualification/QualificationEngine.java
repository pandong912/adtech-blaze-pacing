package com.hotstar.adtech.blaze.allocation.planner.qualification;

import java.util.List;

public interface QualificationEngine<V> {

  List<QualifiedAdSet> qualify(List<V> candidateAdSets);

}
