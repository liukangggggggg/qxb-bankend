package com.qxb.common.minio;
import com.qxb.common.core.dto.UploadResult;
import com.qxb.common.utils.request.RequestUtils;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {


    private  final MinioClient minioClient;
    private final UploadResult uploadResult;

    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${minio.public-endpoint}}")
    private String publicEndpoint;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    public String getBucketName() {
        return bucketName;
    }

    @PostConstruct
    public void init() {
        try {
            createBucketIfNotExists(bucketName);
            log.info("MinIO初始化成功，默认桶: {}", bucketName);
        } catch (Exception e) {
            log.error("MinIO初始化失败", e);
        }
    }

    public void createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("创建桶: {}", bucketName);
        }
    }

    public String uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, bucketName);
    }

    public String uploadFile(MultipartFile file, String bucketName) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        
        log.info("文件上传成功: {}", fileName);
        return fileName;
    }

    public String uploadFile(MultipartFile file, String fileName, String bucketName) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        log.info("文件上传成功: {}", fileName);
        return fileName;
    }

    public String uploadStream(InputStream inputStream, String fileName, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, -1, 10485760)
                        .contentType(contentType)
                        .build()
        );
        log.info("流上传成功: {}", fileName);
        return fileName;
    }
    
    /**
     * 上传字节数组
     */
    public String uploadBytes(byte[] bytes, String fileName, String contentType) throws Exception {
        java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(bytes);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, bytes.length, -1)
                        .contentType(contentType)
                        .build()
        );
        log.info("字节数组上传成功: {}, size={} bytes", fileName, bytes.length);
        return fileName;
    }

    public String getFileUrl(String fileName) throws Exception {
        return getFileUrl(fileName, 7, TimeUnit.DAYS);
    }

    public String getFileUrl(String fileName, int duration, TimeUnit unit) throws Exception {
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(fileName)
                        .expiry(duration, unit)
                        .build()
        );
        
        // 替换为公开访问域名
        url = replaceWithPublicEndpoint(url);
        
        // 强制使用HTTPS，避免混合内容错误
        if (url.startsWith("http://")) {
            url = url.replace("http://", "https://");
            log.debug("URL已转换为HTTPS: {}", url);
        }
        
        return url;
    }
    /**
     * 生成对象存储键名
     *
     * @param file 上传文件
     * @param basePath 基础路径
     * @return 对象存储键名
     */
    public String generateObjectKey(MultipartFile file, String basePath) {
        String extension = getFileExtension(file.getOriginalFilename());
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        return basePath + datePath + "/" + fileName;
    }


    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名（包含点号），如果没有扩展名返回空字符串
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 构建文件访问URL
     *
     * @param objectKey 对象存储键名
     * @param request HTTP请求
     * @return 文件访问URL
     */
    public String buildFileUrl(String objectKey, HttpServletRequest request) {
        String baseUrl = RequestUtils.buildBaseUrl(request, contextPath);
        return baseUrl + "emm/resources/well-serve/" + objectKey;
    }



    /**
     * 构建上传结果
     *
     * @param file 上传文件
     * @param objectKey 对象存储键名
     * @param request HTTP请求
     * @return 上传结果
     */
    public UploadResult buildUploadResult(MultipartFile file, String objectKey, HttpServletRequest request) {
        String fileUrl = this.buildFileUrl(objectKey, request);

        UploadResult result = new UploadResult();
        result.setFileName(objectKey);
        result.setUrl(fileUrl);
        result.setOriginalName(file.getOriginalFilename());

        return result;
    }

    /**
     * 获取文件的永久访问 URL (不带签名,要求桶为公开访问)
     * 适用于头像等需要长期访问的资源
     * 
     * @param fileName 文件名/对象键
     * @return 永久访问 URL
     */
    public String getPublicFileUrl(String fileName) {
        // 使用公开访问域名构建永久 URL
        String url = String.format("%s/%s/%s", publicEndpoint, bucketName, fileName);
        
        log.debug("生成永久URL: {}", url);
        return url;
    }
    
    /**
     * 将内网 endpoint 替换为公开访问域名
     */
    private String replaceWithPublicEndpoint(String url) {
        if (url == null || endpoint.equals(publicEndpoint)) {
            return url;
        }
        
        try {
            // 提取 endpoint 的主机部分（去掉协议）
            String endpointHost = endpoint.replaceFirst("^https?://", "");
            String publicHost = publicEndpoint.replaceFirst("^https?://", "");
            
            // 替换 URL 中的主机部分
            String replaced = url.replaceFirst(endpointHost, publicHost);
            log.debug("URL替换: {} -> {}", url, replaced);
            return replaced;
        } catch (Exception e) {
            log.warn("URL替换失败，使用原始URL: {}", url, e);
            return url;
        }
    }

    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
        log.info("文件删除成功: {}", fileName);
    }

    /**
     * 删除指定前缀下的所有对象（用于“目录式”资源，例如 Unity WebGL）
     */
    public void deleteByPrefix(String prefix) throws Exception {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        String normalized = prefix.startsWith("/") ? prefix.substring(1) : prefix;

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(normalized)
                        .recursive(true)
                        .build()
        );

        List<String> objects = new ArrayList<>();
        for (Result<Item> r : results) {
            Item item = r.get();
            if (item != null && item.objectName() != null) {
                objects.add(item.objectName());
            }
        }

        for (String obj : objects) {
            try {
                deleteFile(obj);
            } catch (Exception e) {
                log.warn("删除对象失败: {}", obj, e);
            }
        }

        log.info("按前缀删除完成: prefix={}, count={}", normalized, objects.size());
    }

    public boolean fileExists(String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public StatObjectResponse getFileInfo(String fileName) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }
    
    /**
     * 下载文件的指定范围（用于视频流式播放）
     * 
     * @param fileName 文件名/对象键
     * @param offset 起始位置
     * @param length 读取长度
     * @return 输入流
     * @throws Exception 下载异常
     */
    public InputStream downloadFileRange(String fileName, long offset, long length) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .offset(offset)
                        .length(length)
                        .build()
        );
    }
}
