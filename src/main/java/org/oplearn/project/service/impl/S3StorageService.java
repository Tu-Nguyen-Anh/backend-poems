package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.FileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3StorageService {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket-name:poems-media}")
  private String bucketName;

  @Value("${aws.s3.endpoint:https://rustfs.tuvidausotoanthu.vn}")
  private String endpoint;

  public String uploadImage(MultipartFile file) {
    return this.uploadFile(file).getUrl();
  }

  public FileResponse uploadFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File upload không được để rỗng");
    }

    try {
      this.createBucketIfNotExists();

      String extension = this.getFileExtension(file.getOriginalFilename());
      String fileName = UUID.randomUUID() + extension;

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentType(file.getContentType())
        .build();


      try (InputStream inputStream = file.getInputStream()) {
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
      }


      log.info("(uploadFile) Upload file lên S3 thành công: {}", fileName);


      String cleanEndpoint = endpoint != null && endpoint.endsWith("/")
        ? endpoint.substring(0, endpoint.length() - 1)
        : endpoint;
      String fileUrl = String.format("%s/%s/%s", cleanEndpoint, bucketName, fileName);


      return FileResponse.builder()
        .url(fileUrl)
        .fileName(fileName)
        .size(file.getSize())
        .contentType(file.getContentType())
        .build();


    } catch (Exception ex) {
      log.error("(uploadFile) Lỗi khi upload file lên S3", ex);
      throw new RuntimeException("Không thể upload file: " + ex.getMessage());
    }
  }

  public void deleteImage(String fileName) {
    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build();

      s3Client.deleteObject(deleteObjectRequest);
      log.info("(deleteImage) Xóa ảnh trên S3 thành công: {}", fileName);
    } catch (Exception ex) {
      log.error("(deleteImage) Lỗi khi xóa ảnh trên S3", ex);
    }
  }


  public List<FileResponse> uploadMultipleFiles(MultipartFile[] files) {
    if (files == null || files.length == 0) {
      throw new IllegalArgumentException("Danh sách file upload không được rỗng");
    }

    List<FileResponse> responses = new ArrayList<>();
    for (MultipartFile file : files) {
      if (!file.isEmpty()) {
        responses.add(this.uploadFile(file)); // Gọi lại hàm upload lẻ từng file
      }
    }
    return responses;
  }

  private void createBucketIfNotExists() {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
    } catch (Exception e) {
      log.warn("(createBucketIfNotExists) HeadBucket không thành công (hoặc bucket chưa tồn tại): {}", e.getMessage());
      try {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        log.info("(createBucket) Đã tạo mới Bucket: {}", bucketName);
      } catch (Exception ex) {
        log.warn("(createBucket) Không thể tạo mới bucket (có thể bucket đã tồn tại): {}", ex.getMessage());
      }

      try {
        String publicPolicy = """
              {
                "Version": "2012-10-17",
                "Statement": [
                  {
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": ["s3:GetObject"],
                    "Resource": ["arn:aws:s3:::%s/*"]
                  }
                ]
              }
              """.formatted(bucketName);

        s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
          .bucket(bucketName)
          .policy(publicPolicy)
          .build());
        log.info("(createBucket) Đã cấu hình Public Access Policy cho Bucket: {}", bucketName);
      } catch (Exception ex) {
        log.warn("(createBucket) Bỏ qua putBucketPolicy (storage provider không hỗ trợ hoặc quyền hạn chế): {}", ex.getMessage());
      }
    }
  }

  private String getFileExtension(String fileName) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(fileName.lastIndexOf("."));
    }
    return ".jpg";
  }
}

