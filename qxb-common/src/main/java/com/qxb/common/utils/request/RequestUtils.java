package com.qxb.common.utils.request;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP请求工具类
 *
 * @author ruoyi
 * @date 2026-03-19
 */
public class RequestUtils {

    /**
     * 构建基础URL
     * <p>从HttpServletRequest中提取协议、域名、端口和上下文路径，构建完整的基础URL
     *
     * @param request HTTP请求
     * @param contextPath 上下文路径
     * @return 基础URL（以/结尾）
     */
    public static String buildBaseUrl(HttpServletRequest request, String contextPath) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String portPart = (serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort;
        String baseUrl = scheme + "://" + serverName + portPart + contextPath;
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }
}
