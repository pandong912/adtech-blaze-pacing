package com.hotstar.adtech.blaze.allocationplan.client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.exception.BusinessException;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.SupplyInfo;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import com.hotstar.adtech.blaze.allocationplan.client.util.ProtostuffUtils;
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
    String fileName = allocationPlan.getTags() + "|" + md5;
    doUpload(file, path, fileName);
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
  public UploadResult uploadHwmPlan(String path, HwmAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    String fileName = allocationPlan.getTags() + "|" + md5;
    doUpload(file, path, fileName);
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
  public void uploadSupplyIdMap(String path, Map<String, Integer> supplyIdMap) {
    SupplyInfo supplyInfo = SupplyInfo.builder()
      .supplyIdMap(supplyIdMap)
      .build();
    byte[] file = ProtostuffUtils.serialize(supplyInfo);
    doUpload(file, path, SUPPLY_ID_MAP_FILE_NAME);
  }


  @Override
  public Map<Long, ShaleAllocationPlan> loadShaleAllocationPlans(PlanType planType,
                                                                 List<LoadRequest> loadRequests) {
    List<LoadRequest> selectedPlan = loadRequests.stream()
      .filter(loadRequest -> planType == loadRequest.getPlanType())
      .filter(loadRequest -> AlgorithmType.SHALE == loadRequest.getAlgorithmType())
      .collect(Collectors.toList());
    return selectedPlan.stream()
      .collect(Collectors.toMap(LoadRequest::getPlanId,
        l -> loadFromS3(ShaleAllocationPlan.class, l.getPath(), l.getFileName())));
  }

  @Override
  public Map<Long, HwmAllocationPlan> loadHwmAllocationPlans(PlanType planType,
                                                             List<LoadRequest> loadRequests) {
    List<LoadRequest> selectedPlan = loadRequests.stream()
      .filter(loadRequest -> planType == loadRequest.getPlanType())
      .filter(loadRequest -> AlgorithmType.HWM == loadRequest.getAlgorithmType())
      .collect(Collectors.toList());
    return selectedPlan.stream()
      .collect(Collectors.toMap(LoadRequest::getPlanId,
        l -> loadFromS3(HwmAllocationPlan.class, l.getPath(), l.getFileName())));
  }

  @Override
  public SupplyInfo loadSupplyIdMap(String path) {
    return loadFromS3(SupplyInfo.class, path, SUPPLY_ID_MAP_FILE_NAME);
  }

  private void doUpload(byte[] file, String path, String fileName) {
    String key = Paths.get(path, fileName).toString();
    try (InputStream is = new ByteArrayInputStream(file)) {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.length);
      s3Client.putObject(this.bucketName, key, is, metadata);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_UPLOAD_FAILED, e, bucketName + "/" + key);
    }
  }

  private <T> T loadFromS3(Class<T> clazz, String path, String fileName) {
    String key =
      Paths.get(path, fileName).toString();
    try (InputStream is = loadFromS3(key)) {
      byte[] bytes = IOUtils.toByteArray(is);
      return ProtostuffUtils.deserialize(bytes, clazz);
    } catch (Exception e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_LOAD_FAILED, e, bucketName + "/" + key);
    }
  }

  private InputStream loadFromS3(String keyName) {
    S3Object s3Object = s3Client.getObject(bucketName, keyName);
    S3ObjectInputStream s3is = s3Object.getObjectContent();
    return new BufferedInputStream(s3is, S3_LOAD_BUF_SIZE);
  }
}
