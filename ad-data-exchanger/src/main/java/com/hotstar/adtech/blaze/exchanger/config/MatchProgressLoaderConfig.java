package com.hotstar.adtech.blaze.exchanger.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.hotstar.adtech.blaze.exchanger.algorithm.LocalMatchProgressLoader;
import com.hotstar.adtech.blaze.exchanger.algorithm.MatchProgressLoader;
import com.hotstar.adtech.blaze.exchanger.algorithm.S3MatchProgressLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MatchProgressLoaderConfig {

  @Value("${blaze.match-progress-loader.s3-region}")
  private String s3Region;
  @Value("${blaze.match-progress-loader.bucket-name}")
  private String bucketName;

  @Value("${blaze.match-progress-loader.path}")
  private String path;

  @Bean(name = "breakProgressAdModelClient")
  @Profile("!local")
  public MatchProgressLoader breakProgressAdModelClient() {
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
      .withRegion(s3Region)
      .withClientConfiguration(new ClientConfiguration().withMaxConnections(1))
      .build();
    return new S3MatchProgressLoader(s3Client, bucketName, path);
  }

  @Bean(name = "breakProgressAdModelClient")
  @Profile("local")
  public MatchProgressLoader localBreakProgressAdModelClient() {
    return new LocalMatchProgressLoader();
  }
}