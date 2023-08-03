package com.hotstar.adtech.blaze.allocation.planner.metric;

public class MetricNames {
  private static final String PREFIX = "blaze.";
  private static final String ALLOCATION_PLAN = "allocation.plan.";
  public static final String MATCH_TOTAL_BREAK_FETCH = PREFIX + "match.total.break.fetch";
  public static final String MATCH_IMPRESSION_FETCH = PREFIX + "match.impression.fetch";
  public static final String MATCH_PLAN_UPDATE = PREFIX + "match.plan.update";
  public static final String GRAPH_SOLVE = PREFIX + ALLOCATION_PLAN + "graph.solve";
  public static final String AD_SET_DIAGNOSIS_BUILD = PREFIX + "ad.set.diagnosis.build";
  public static final String QUALIFICATION = PREFIX + ALLOCATION_PLAN + "qualification";
  public static final String MATCH_REACH_FETCH = PREFIX + "match.reach.fetch";
  public static final String MATCH_CONCURRENCY_FETCH = PREFIX + "match.concurrency.fetch";
  public static final String REDIS_TIMEOUT_EXCEPTION = PREFIX + "redis.timeout.exception";
  public static final String REDIS_OTHER_EXCEPTION = PREFIX + "redis.other.exception";

  public static final String PLAN_DATA_LOAD = PREFIX + ALLOCATION_PLAN + "data.load";
  public static final String BUILD_FAILING = PREFIX + ALLOCATION_PLAN + "build.failing";
  public static final String WORKER = PREFIX + ALLOCATION_PLAN + "worker";
  public static final String GENERATOR = PREFIX + ALLOCATION_PLAN + "generator";
  public static final String RESULT = PREFIX + ALLOCATION_PLAN + "result";
  public static final String DIAGNOSIS = PREFIX + ALLOCATION_PLAN + "diagnosis";
  public static final String STAGE_ONG_LOOP = PREFIX + ALLOCATION_PLAN + "stage.one.loop";
}

