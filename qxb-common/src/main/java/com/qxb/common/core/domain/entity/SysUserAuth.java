package com.qxb.common.core.domain.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.qxb.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserAuth extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long authId;

    private Long userId;

    @NotNull(message = "认证类型不能为空")
    @NotBlank(message = "认证类型不能为空白")
    @Size(max = 50, message = "认证类型长度不能超过50个字符")
    private String identityType;

    @NotNull(message = "唯一标识不能为空")
    @Size(max = 200, message = "标识符长度不能超过200个字符")
    private String identifier;

    @Size(max = 100, message = "Union ID长度不能超过100个字符")
    private String unionId;

    @Size(max = 500, message = "凭证长度不能超过500个字符")
    private String credential;

    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "登录IP格式不正确")
    private String loginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;


}
