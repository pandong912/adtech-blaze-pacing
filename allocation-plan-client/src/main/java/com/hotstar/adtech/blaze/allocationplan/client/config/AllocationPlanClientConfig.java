package com.hotstar.adtech.blaze.allocationplan.client.config;

import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.LocalAllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.S3AllocationPlanClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AllocationPlanClientConfig {

  @Configuration
  @Profile("!local")
  public static class S3ClientConfiguration {

    @Value("${blaze.allocation-planner-client.aws.s3.region}")
    private String s3Region;
    @Value("${blaze.allocation-planner-client.aws.s3.bucket-name}")
    private String bucketName;

    @Bean
    public AllocationPlanClient allocationPlanClient() {
      return new S3AllocationPlanClient(s3Region, bucketName);
    }
  }

  @Configuration
  @Profile("local")
  public static class LocalClientConfiguration {
    @Value("${blaze.allocation-planner-client.local.base-dir:./}")
    private String baseDir;


    @Bean
    public AllocationPlanClient allocationPlanClient() {
      return new LocalAllocationPlanClient(baseDir);

    }
  }
}
