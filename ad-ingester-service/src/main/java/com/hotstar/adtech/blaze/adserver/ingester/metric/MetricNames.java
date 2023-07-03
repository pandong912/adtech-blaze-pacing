package com.hotstar.adtech.blaze.adserver.ingester.metric;

public class MetricNames {
  private static final String PREFIX = "blaze.";
  public static final String CONCURRENCY_UPDATE_EXCEPTION = PREFIX + "concurrency.update.exception";
  public static final String IMPRESSION_UPDATE_EXCEPTION = PREFIX + "impression.update.exception";

  public static final String CONTENT_CONCURRENCY_SYNC = PREFIX + "content.concurrency.sync";
  public static final String AD_SET_IMPRESSION_SYNC = PREFIX + "ad.set.impression.sync";

  public static final String LOAD_AD_MODEL_STATUS = PREFIX + "load.ad.model.status";
  public static final String LOAD_AD_MODEL = PREFIX + "load.ad.model";
  public static final String INVALID_CONCURRENCY = PREFIX + "ingester.concurrency.invalid";
  public static final String TOTAL_CONCURRENCY = PREFIX + "ingester.concurrency.total";

}
