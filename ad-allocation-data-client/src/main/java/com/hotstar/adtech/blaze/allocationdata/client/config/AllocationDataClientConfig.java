package com.hotstar.adtech.blaze.allocationdata.client.config;

import com.hotstar.adtech.blaze.allocationdata.client.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationdata.client.LocalAllocationDataClient;
import com.hotstar.adtech.blaze.allocationdata.client.S3AllocationDataClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AllocationDataClientConfig {

  @Configuration
  @Profile("!local")
  public static class S3DataClientConfiguration {

    @Value("${blaze.allocation-data-client.aws.s3.region}")
    private String s3Region;
    @Value("${blaze.allocation-data-client.aws.s3.bucket-name}")
    private String bucketName;

    @Bean
    public AllocationDataClient allocationDataClient() {
      return new S3AllocationDataClient(s3Region, bucketName);
    }
  }

  @Configuration
  @Profile("local")
  public static class LocalDataClientConfiguration {

    @Value("${blaze.allocation-data-client.local.base-dir:./}")
    private String baseDir;

    @Bean
    public AllocationDataClient allocationDataClient() {
      return new LocalAllocationDataClient(baseDir);
    }
  }
}
