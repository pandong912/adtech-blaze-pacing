package com.hotstar.adtech.blaze.allocationplan.client;

import com.hotstar.adtech.blaze.allocationplan.client.common.GzipUtils;
import org.junit.Assert;
import org.junit.Test;

public class AllocationPlanTests {

  @Test
  public void gzipTest() {
    String s = "testAllocationPlanClient";
    byte[] compressed = GzipUtils.compress(s.getBytes());
    byte[] decompressed = GzipUtils.decompress(compressed);
    System.out.println(new String(decompressed));
    Assert.assertEquals(s, new String(decompressed));
  }
}
