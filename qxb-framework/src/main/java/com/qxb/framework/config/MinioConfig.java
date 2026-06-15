package com.qxb.framework.config;

import com.qxb.common.config.MinioProperties;
import io.minio.MinioClient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * MinIO配置类
 */
@EnableConfigurationProperties(MinioProperties.class)
@Configuration
@RequiredArgsConstructor
public class MinioConfig {
    private final MinioProperties minioProperties;
//    /**
//     * MinIo服务端点地址
//     * example:https://minio.eample.com:9000
//     */
//    @NotBlank(message = "MinIO endpoint 不能为空")
//    private String endpoint;
//    /**
//     * 访问密钥（Access Key）
//     */
//    @NotBlank(message = "MinIO accessKey 不能为空")
//    private String accessKey;
//    /**
//     * 秘密密钥（Secret Key）
//     */
//    @NotBlank(message = "MinIO secretKey 不能为空")
//    private String secretKey;
///**
// * 存储桶名称
// */
//@NotBlank(message = "MinIO bucketName 不能为空")
//    private String bucketName = "files";
//
//    /**
//     * 是否新人所有证书
//     */
//    private boolean trustAllCertificates=false;
//    /**
//     * 连接超时时间
//     */
//    private Duration connectTimeout = Duration.ofSeconds(10);
//    /**
//     * 读取超时时间
//     */
//    private Duration readTimeout = Duration.ofSeconds(30);
//    /**
//     * 写入超时时间
//     */
//    private Duration writeTimeout = Duration.ofSeconds(30);
    
    @Bean
    public MinioClient minioClient() {
//        try {
//            // 创建信任所有证书的TrustManager
//            TrustManager[] trustAllCerts = new TrustManager[]{
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
//                    }
//
//                    @Override
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return new X509Certificate[]{};
//                    }
//                }
//            };
//
//            // 创建SSLContext
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//
//            // 创建OkHttpClient，信任所有证书
//            OkHttpClient httpClient = new OkHttpClient.Builder()
//                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
//                    .hostnameVerifier((hostname, session) -> true)
//                    .build();
//
//            return MinioClient.builder()
//                    .endpoint(endpoint)
//                    .credentials(accessKey, secretKey)
//                    .httpClient(httpClient)
//                    .build();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create MinIO client", e);
//        }
        try {
            return MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .httpClient(buildHttpClient())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("创建 MinIO 客户端失败，endpoint=" + minioProperties.getEndpoint(), e);
        }
    }
    private OkHttpClient buildHttpClient() throws Exception {
return new OkHttpClient.Builder()
        .connectTimeout(minioProperties.getConnectTimeout())
        .readTimeout(minioProperties.getReadTimeout())
        .writeTimeout(minioProperties.getWriteTimeout())
        .build();
    }
    private X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
}
