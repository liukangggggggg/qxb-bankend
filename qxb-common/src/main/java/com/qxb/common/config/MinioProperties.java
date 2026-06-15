package com.qxb.common.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * MinIO 配置属性
 */
@Data
@Validated
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    @NotBlank(message = "MinIO endpoint 不能为空")
    private String endpoint;

    @NotBlank(message = "MinIO accessKey 不能为空")
    private String accessKey;

    @NotBlank(message = "MinIO secretKey 不能为空")
    private String secretKey;

    @NotBlank(message = "MinIO bucketName 不能为空")
    private String bucketName = "files";

    /**
     * 外部访问地址，不配置时默认使用 endpoint
     */
    private String publicEndpoint;

    private boolean trustAllCertificates = false;

    private Duration connectTimeout = Duration.ofSeconds(10);

    private Duration readTimeout = Duration.ofSeconds(30);

    private Duration writeTimeout = Duration.ofSeconds(30);

    public String getPublicEndpoint() {
        return publicEndpoint == null || publicEndpoint.isBlank() ? endpoint : publicEndpoint;
    }
}