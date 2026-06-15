package com.qxb.common.core.domain.entity;
import java.io.Serial;
import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.qxb.common.annotation.Excel;
import com.qxb.common.annotation.Excel.ColumnType;
import com.qxb.common.annotation.Excel.Type;
import com.qxb.common.annotation.Excels;
import com.qxb.common.core.domain.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysUser extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;


    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    @Size(max = 50, message = "用户昵称长度不能超过50个字符")
    @Excel(name = "用户昵称")
    private String nickName;

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Excel(name = "真实姓名")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Excel(name = "用户邮箱")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Excel(name = "手机号码")
    private String phoneNumber;

    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    @Size(max = 200, message = "头像地址长度不能超过200个字符")
    private String avatar;

    @Size(max = 100, message = "所在城市长度不能超过100个字符")
    @Excel(name = "所在城市")
    private String city;


    @Size(max = 1, message = "状态长度不能超过1个字符")
    private String status;

    @Size(max = 1, message = "删除标志长度不能超过1个字符")
    private String delFlag;

    @Size(max = 2000, message = "扩展信息长度不能超过2000个字符")
    private String extInfo;

    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    private SysDept dept;

    private List<SysRole> roles;

    private Long[] roleIds;

    private Long roleId;



}
