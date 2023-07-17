package com.hotstar.adtech.blaze.exchanger.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash.DistributionsLoader;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash.LocalDistributionsLoader;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.adcrash.S3DistributionsLoader;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress.LocalMatchProgressLoader;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress.MatchProgressLoader;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress.S3MatchProgressLoader;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

  @Bean
  public GracefulShutdown gracefulShutdown() {
    return new GracefulShutdown();
  }

  @Bean
  public ConfigurableServletWebServerFactory webServerFactory(
    final GracefulShutdown gracefulShutdown) {
    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    factory.addConnectorCustomizers(gracefulShutdown);
    return factory;
  }

  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry, ignored -> Collections.emptyList());
  }

  @Bean
  @Profile("!local")
  public DistributionsLoader s3DistributionLoader(
      @Value("${blaze.ad-crash-model.s3.region}") String region,
      @Value("${blaze.ad-crash-model.s3.bucket}") String bucket,
      @Value("${blaze.ad-crash-model.s3.path}") String path) {
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withClientConfiguration(new ClientConfiguration().withMaxConnections(1))
        .build();
    return new S3DistributionsLoader(s3Client, bucket, path);
  }

  @Bean
  @Profile("!local")
  public MatchProgressLoader s3MatchProgressLoader(
      @Value("${blaze.match-progress-model.s3.region}") String region,
      @Value("${blaze.match-progress-model.s3.bucket}") String bucket,
      @Value("${blaze.match-progress-model.s3.path}") String path) {
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withClientConfiguration(new ClientConfiguration().withMaxConnections(1))
        .build();
    return new S3MatchProgressLoader(s3Client, bucket, path);
  }

  @Bean
  @Profile("local")
  public DistributionsLoader localDistributionLoader() {
    return new LocalDistributionsLoader();
  }

  @Bean
  @Profile("local")
  public MatchProgressLoader localMatchProgressLoader() {
    return new LocalMatchProgressLoader();
  }

}
