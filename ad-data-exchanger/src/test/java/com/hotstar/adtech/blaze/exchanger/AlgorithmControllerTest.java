
package com.hotstar.adtech.blaze.exchanger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.exchanger.algorithmmodel.matchprogress.S3MatchProgressLoader;
import com.hotstar.adtech.blaze.exchanger.api.response.MatchProgressModelResponse;
import com.hotstar.adtech.blaze.exchanger.controller.AlgorithmController;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AlgorithmControllerTest extends TestEnvConfig {
  static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:0.11.3");
  public static final LocalStackContainer localStackContainer = new LocalStackContainer(localstackImage)
    .withServices(LocalStackContainer.Service.S3);

  static {
    localStackContainer.start();
  }

  @Autowired
  private AlgorithmController algorithmController;

  private static final String testBucketName = "match-break-progress";
  private static final String path = "break-progress/{date}.csv";
  private String currentDay;

  @BeforeAll
  public void setup() throws IOException {
    AmazonS3 s3Client = getS3Client();
    ClassPathResource csv = new ClassPathResource("s3/match-break-progress.csv");
    s3Client.createBucket(testBucketName);
    currentDay = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String key = path.replace("{date}", currentDay);
    s3Client.putObject(testBucketName, key, csv.getInputStream(), null);
  }

  @Test
  public void testLatestGetMatchBreakProgressModel() {
    StandardResponse<MatchProgressModelResponse> matchBreakProgressModel =
        algorithmController.getLatestMatchBreakProgressModel();
    System.out.println(matchBreakProgressModel.getData());
    Assertions.assertEquals(5, matchBreakProgressModel.getData().getDeliveryProgresses().size());
    Assertions.assertEquals(0.2, matchBreakProgressModel.getData().getDeliveryProgresses().get(0), 0.0);
  }

  @Test
  public void testGetMatchBreakProgressModelByDate() {
    StandardResponse<MatchProgressModelResponse> matchBreakProgressModel =
        algorithmController.getMatchBreakProgressModel(currentDay);
    System.out.println(matchBreakProgressModel.getData());
    Assertions.assertEquals(5, matchBreakProgressModel.getData().getDeliveryProgresses().size());
    Assertions.assertEquals(0.2, matchBreakProgressModel.getData().getDeliveryProgresses().get(0), 0.0);
  }

  private static AmazonS3 getS3Client() {
    return AmazonS3ClientBuilder
      .standard()
      .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(
          localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
          localStackContainer.getRegion()))
      .withCredentials(new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
      .build();
  }

  @TestConfiguration
  static class S3LoaderConfiguration {
    @Bean
    @Primary
    public S3MatchProgressLoader s3MatchProgressLoader() {
      AmazonS3 s3Client = getS3Client();
      return new S3MatchProgressLoader(s3Client, testBucketName, path);
    }
  }

  @AfterAll
  public void tearDown() {
    localStackContainer.stop();
  }
}
