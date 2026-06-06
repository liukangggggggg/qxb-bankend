package com.qxb.web.controller.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.qxb.common.core.controller.BaseController;
import com.qxb.common.core.domain.AjaxResult;
import com.qxb.common.core.domain.entity.SysUser;
import com.qxb.common.core.domain.model.LoginUser;
import com.qxb.common.core.domain.model.SmsSendBody;
import com.qxb.common.enums.SmsCodeType;
import com.qxb.common.utils.MessageUtils;
import com.qxb.common.utils.SecurityUtils;
import com.qxb.common.utils.StringUtils;
import com.qxb.framework.web.service.SmsCodeService;
import com.qxb.system.service.ISysUserService;

/**
 * 短信验证码
 *
 * @author qxb
 */
@RestController
public class SmsController extends BaseController
{
    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private ISysUserService userService;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public AjaxResult send(@RequestBody SmsSendBody body)
    {
        if (StringUtils.isEmpty(body.getType()))
        {
            return error("验证码类型不能为空");
        }
        SmsCodeType type;
        try
        {
            type = SmsCodeType.fromCode(body.getType());
        }
        catch (IllegalArgumentException e)
        {
            return error("无效的验证码类型");
        }
        smsCodeService.validatePhone(body.getPhone());
        if (type == SmsCodeType.LOGIN)
        {
            SysUser user = userService.selectUserByPhonenumber(body.getPhone());
            if (StringUtils.isNull(user))
            {
                return error(MessageUtils.message("user.phone.not.bound"));
            }
        }
        else if (type == SmsCodeType.BIND)
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            SysUser checkUser = new SysUser();
            checkUser.setUserId(loginUser.getUserId());
            checkUser.setPhonenumber(body.getPhone());
            if (!userService.checkPhoneUnique(checkUser))
            {
                return error("该手机号已被其他账号绑定");
            }
        }
        smsCodeService.sendCode(body.getPhone(), type);
        return success("验证码发送成功");
    }
}
