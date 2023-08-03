package com.hotstar.adtech.blaze.allocation.planner.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  public static final ObjectMapper MAPPER =
    new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
      .findAndRegisterModules()
      .registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT))
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Bean
  public ObjectMapper jacksonObjectMapper() {
    return MAPPER;
  }
}
