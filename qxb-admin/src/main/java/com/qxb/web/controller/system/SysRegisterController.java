package com.qxb.web.controller.system;

import com.qxb.common.annotation.Anonymous;
import com.qxb.common.core.domain.model.RegisterBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.qxb.common.core.controller.BaseController;
import com.qxb.common.core.domain.AjaxResult;
import com.qxb.common.core.domain.model.PhoneRegisterBody;
import com.qxb.common.core.domain.model.WechatLoginBody;
import com.qxb.common.utils.StringUtils;
import com.qxb.framework.web.service.SysLoginService;
import com.qxb.framework.web.service.SysRegisterService;
import com.qxb.system.service.ISysConfigService;

/**
 * 用户注册控制器
 * 
 * @author ruoyi
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 用户注册
     * 
     * @param user 用户注册信息
     * @return 注册结果
     */
    @Anonymous
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 手机号注册
     * 
     * @param body 手机号注册信息
     * @return 注册结果
     */
    @PostMapping("/register/phone")
    public AjaxResult phoneRegister(@RequestBody PhoneRegisterBody body)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.phoneRegister(body);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 手机号验证码登录
     * 
     * @param body 手机号登录信息
     * @return 登录结果
     */
    @PostMapping("/register/phone/login")
    public AjaxResult phoneLogin(@RequestBody PhoneRegisterBody body)
    {
        if (StringUtils.isEmpty(body.getPhone()))
        {
            return error("手机号不能为空");
        }
        if (StringUtils.isEmpty(body.getSmsCode()))
        {
            return error("短信验证码不能为空");
        }
        String token = loginService.phoneLoginOrRegister(body.getPhone(), body.getSmsCode());
        return success(token);
    }

    /**
     * 微信登录
     * 
     * @param body 微信登录信息
     * @return 登录结果
     */
    @PostMapping("/login/wechat")
    public AjaxResult wechatLogin(@RequestBody WechatLoginBody body)
    {
        if (StringUtils.isEmpty(body.getCode()))
        {
            return error("微信授权码不能为空");
        }
        String token = loginService.wechatLogin(body.getCode());
        return success(token);
    }
}
