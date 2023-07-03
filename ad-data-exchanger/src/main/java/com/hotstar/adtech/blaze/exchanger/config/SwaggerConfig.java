package com.hotstar.adtech.blaze.exchanger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Import({springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration.class})
public class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
      .enable(true)
      .select()
      .apis(RequestHandlerSelectors.basePackage("com.hotstar.adtech.blaze.exchanger.controller"))
      .paths(PathSelectors.any())
      .build()
      .apiInfo(apiEndPointsInfo())
      .useDefaultResponseMessages(false);
  }

  private ApiInfo apiEndPointsInfo() {
    return new ApiInfoBuilder().title("Blaze Ad Exchanger Service API")
      .description("Blaze Ad Exchanger Service Interface Documentation")
      .contact(new Contact("pandong-hotstar",
        "https://github.com/hotstar/adtech-blaze-lcm-backend.git",
        "dong.pan@hotstar.com"))
      .license("The MIT License")
      .licenseUrl("https://opensource.org/licenses/MIT")
      .version("V1")
      .build();
  }

}
