package com.hotstar.adtech.blaze.allocation.planner.util;

import java.util.List;

public class MemoryAlignment {
  public static <T> int getSize(List<T> list) {
    int floor = list.size() / 64;
    return (list.size() % 64 == 0 ? floor : floor + 1) * 64;
  }
}