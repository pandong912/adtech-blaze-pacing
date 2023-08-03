package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector;

public interface Inspector<T> {

  boolean qualify(T ad);

}
