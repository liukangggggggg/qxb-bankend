package com.qxb.common.core.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 轮播图上传结果 DTO
 * 
 * @author ruoyi
 * @date 2026-03-19
 */
@Component
@Data
public class UploadResult {
    
    /**
     * 文件在 MinIO 中的对象键（路径）
     */
    private String fileName;
    
    /**
     * 文件访问 URL
     */
    private String url;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 视频缩略图对象Key
     */
    private String thumbnailKey;
    
    /**
     * 视频缩略图URL
     */
    private String thumbnailUrl;
}
