package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector;

public interface Inspector<V> {

  boolean qualify(V adSet);

}
