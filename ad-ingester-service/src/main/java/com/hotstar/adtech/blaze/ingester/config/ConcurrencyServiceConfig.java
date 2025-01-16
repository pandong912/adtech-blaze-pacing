package com.hotstar.adtech.blaze.ingester.config;

import com.hotstar.adtech.blaze.ingester.launchdarkly.LdConfig;
import com.hotstar.adtech.blaze.ingester.repository.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.repository.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.service.ConcurrencyService;
import com.hotstar.adtech.blaze.ingester.service.InjectConcurrencyService;
import com.hotstar.adtech.blaze.ingester.service.PulseService;
import com.hotstar.platform.pulse.api.PulseClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConcurrencyServiceConfig {

  @Bean
  public PulseService pulseService(PulseClient pulseClient) {
    return new PulseService(pulseClient);
  }

  @Bean
  @ConditionalOnProperty(value = "blaze.ad-ingester-service.concurrency.inject.enable", havingValue = "true")
  public InjectConcurrencyService injectConcurrencyService(
    StreamConcurrencyRepository streamConcurrencyRepository,
    StreamCohortConcurrencyRepository streamCohortConcurrencyRepository,
    PulseService pulseService, LdConfig ldConfig) {
    return new InjectConcurrencyService(pulseService, streamConcurrencyRepository, streamCohortConcurrencyRepository,
      ldConfig);
  }

  @Bean
  @ConditionalOnMissingBean(ConcurrencyService.class)
  ConcurrencyService concurrencyService(
    StreamConcurrencyRepository streamConcurrencyRepository,
    StreamCohortConcurrencyRepository streamCohortConcurrencyRepository,
    PulseService pulseService, LdConfig ldConfig) {
    return new ConcurrencyService(pulseService, streamConcurrencyRepository, streamCohortConcurrencyRepository,
      ldConfig);
  }

}
