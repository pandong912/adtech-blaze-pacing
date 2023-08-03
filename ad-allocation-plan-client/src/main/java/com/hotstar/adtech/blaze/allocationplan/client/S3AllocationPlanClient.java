package com.hotstar.adtech.blaze.allocationplan.client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.AllocationDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.SupplyInfo;
import com.hotstar.adtech.blaze.allocationplan.client.common.GzipUtils;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import com.hotstar.adtech.blaze.allocationplan.client.common.ProtostuffUtils;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

@Slf4j
@SuppressWarnings("unused")
public class S3AllocationPlanClient implements AllocationPlanClient {

  private static final String SUPPLY_ID_MAP_FILE_NAME = "streamCohortToSupplyId";
  private static final int S3_LOAD_BUF_SIZE = 1024 * 4;
  private static final int MAX_CONNECTIONS = 6;

  private final String bucketName;
  private final AmazonS3 s3Client;

  public S3AllocationPlanClient(String s3Region, String bucketName) {
    this.bucketName = bucketName;
    this.s3Client = AmazonS3ClientBuilder.standard()
      .withRegion(s3Region)
      .withClientConfiguration(new ClientConfiguration().withMaxConnections(MAX_CONNECTIONS))
      .build();
  }

  @Override
  public UploadResult uploadShalePlan(String path, ShaleAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    String fileName = doUpload(file, path, md5);
    return UploadResult.builder()
      .breakTypeIds(allocationPlan.getBreakTypeIds())
      .algorithmType(AlgorithmType.SHALE)
      .planType(PlanType.SSAI)
      .duration(allocationPlan.getDuration())
      .nextBreakIndex(allocationPlan.getNextBreakIndex())
      .totalBreakNumber(allocationPlan.getTotalBreakNumber())
      .fileName(fileName)
      .md5(md5)
      .build();
  }

  @Override
  public List<UploadResult> batchUploadShalePlan(String path, List<ShaleAllocationPlan> allocationPlans) {
    return allocationPlans.stream()
      .map(allocationPlan -> uploadShalePlan(path, allocationPlan))
      .collect(Collectors.toList());
  }

  @Override
  public UploadResult uploadHwmPlan(String path, HwmAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    String fileName = doUpload(file, path, md5);
    return UploadResult.builder()
      .breakTypeIds(allocationPlan.getBreakTypeIds())
      .planType(allocationPlan.getPlanType())
      .algorithmType(AlgorithmType.HWM)
      .duration(allocationPlan.getDuration())
      .nextBreakIndex(allocationPlan.getNextBreakIndex())
      .totalBreakNumber(allocationPlan.getTotalBreakNumber())
      .fileName(fileName)
      .md5(md5)
      .build();
  }

  @Override
  public List<UploadResult> batchUploadHwmPlan(String path, List<HwmAllocationPlan> allocationPlans) {
    return allocationPlans.stream()
      .map(allocationPlan -> uploadHwmPlan(path, allocationPlan))
      .collect(Collectors.toList());
  }

  @Override
  public void uploadSupplyIdMap(String path, Map<String, Integer> supplyIdMap) {
    SupplyInfo supplyInfo = SupplyInfo.builder()
      .supplyIdMap(supplyIdMap)
      .build();
    byte[] file = ProtostuffUtils.serialize(supplyInfo);
    doUpload(file, path, SUPPLY_ID_MAP_FILE_NAME);
  }


  @Override
  public List<ShaleAllocationPlan> loadShaleAllocationPlans(List<LoadRequest> loadRequests) {
    return loadRequests.stream()
      .map(loadRequest -> loadFromS3(ShaleAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName()))
      .collect(Collectors.toList());
  }

  @Override
  public List<HwmAllocationPlan> loadHwmAllocationPlans(List<LoadRequest> loadRequests) {
    return loadRequests.stream()
      .map(loadRequest -> loadFromS3(HwmAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName()))
      .collect(Collectors.toList());
  }

  @Override
  public ShaleAllocationPlan loadShaleAllocationPlan(LoadRequest loadRequest) {
    return loadFromS3(ShaleAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName());
  }

  @Override
  public HwmAllocationPlan loadHwmAllocationPlan(LoadRequest loadRequest) {
    return loadFromS3(HwmAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName());
  }

  @Override
  public SupplyInfo loadSupplyIdMap(String path) {
    return loadFromS3(SupplyInfo.class, path, SUPPLY_ID_MAP_FILE_NAME);
  }

  private String doUpload(byte[] file, String path, String fileName) {
    String key = Paths.get(path, fileName).toString();
    try (InputStream is = new ByteArrayInputStream(file)) {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.length);
      s3Client.putObject(this.bucketName, key, is, metadata);
    } catch (IOException e) {
      throw new ServiceException("Failed to upload allocation plan to:" + bucketName + "/" + key, e);
    }
    return fileName;
  }

  private <T> T loadFromS3(Class<T> clazz, String path, String fileName) {
    String key =
      Paths.get(path, fileName).toString();
    try (InputStream is = loadFromS3(key)) {
      byte[] bytes = IOUtils.toByteArray(is);
      return ProtostuffUtils.deserialize(bytes, clazz);
    } catch (Exception e) {
      throw new ServiceException("Failed to load allocation plan from:" + bucketName + "/" + key, e);
    }
  }

  private InputStream loadFromS3(String keyName) {
    S3Object s3Object = s3Client.getObject(bucketName, keyName);
    S3ObjectInputStream s3is = s3Object.getObjectContent();
    return new BufferedInputStream(s3is, S3_LOAD_BUF_SIZE);
  }


  public void uploadAllocationDiagnosis(AllocationDiagnosis allocationDiagnosis) {
    String path = Paths.get(PathUtils.joinToDiagnosisPath(allocationDiagnosis.getContentId(),
      allocationDiagnosis.getVersion())).toString();
    try {
      byte[] value = ProtostuffUtils.serialize(allocationDiagnosis);
      byte[] compressed = GzipUtils.compress(value);
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(compressed.length);
      ByteArrayInputStream buf = new ByteArrayInputStream(compressed);
      s3Client.putObject(this.bucketName, path, buf, metadata);
    } catch (Exception e) {
      throw new ServiceException("fail upload allocation diagnosis to:" + bucketName + "/" + path, e);
    }
  }

  public AllocationDiagnosis loadAllocationDiagnosis(String path) {
    try {
      S3Object s3Object = s3Client.getObject(bucketName, path);
      S3ObjectInputStream s3is = s3Object.getObjectContent();
      BufferedInputStream buf = new BufferedInputStream(s3is, S3_LOAD_BUF_SIZE);
      byte[] bytes = IOUtils.toByteArray(buf);
      byte[] decompressed = GzipUtils.decompress(bytes);
      return ProtostuffUtils.deserialize(decompressed, AllocationDiagnosis.class);
    } catch (Exception e) {
      throw new ServiceException("fail get allocation diagnosis from:" + bucketName + "/" + path, e);
    }
  }
}
