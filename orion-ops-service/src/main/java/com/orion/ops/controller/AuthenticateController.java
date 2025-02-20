package com.orion.ops.controller;

import com.orion.lang.wrapper.HttpWrapper;
import com.orion.ops.annotation.EventLog;
import com.orion.ops.annotation.IgnoreAuth;
import com.orion.ops.annotation.RestWrapper;
import com.orion.ops.consts.event.EventType;
import com.orion.ops.entity.request.UserLoginRequest;
import com.orion.ops.entity.request.UserResetRequest;
import com.orion.ops.entity.vo.UserInfoVO;
import com.orion.ops.entity.vo.UserLoginVO;
import com.orion.ops.service.api.PassportService;
import com.orion.ops.utils.Currents;
import com.orion.ops.utils.Valid;
import com.orion.servlet.web.Servlets;
import com.orion.utils.Objects1;
import com.orion.utils.convert.Converts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户认证 api
 *
 * @author Jiahang Li
 * @version 1.0.0
 * @since 2021/4/1 17:05
 */
@Api(tags = "用户认证")
@RestController
@RestWrapper
@RequestMapping("/orion/api/auth")
public class AuthenticateController {

    @Resource
    private PassportService passportService;

    @IgnoreAuth
    @PostMapping("/login")
    @ApiOperation(value = "登录")
    @EventLog(EventType.LOGIN)
    public UserLoginVO login(@RequestBody UserLoginRequest login, HttpServletRequest request) {
        String username = Valid.notBlank(login.getUsername()).trim();
        String password = Valid.notBlank(login.getPassword()).trim();
        login.setUsername(username);
        login.setPassword(password);
        login.setIp(Servlets.getRemoteAddr(request));
        // 登录
        return passportService.login(login);
    }

    @IgnoreAuth
    @PostMapping("/logout")
    @ApiOperation(value = "登出")
    @EventLog(EventType.LOGOUT)
    public HttpWrapper<?> logout() {
        passportService.logout();
        return HttpWrapper.ok();
    }

    @PostMapping("/reset")
    @ApiOperation(value = "重置密码")
    @EventLog(EventType.RESET_PASSWORD)
    public HttpWrapper<?> resetPassword(@RequestBody UserResetRequest request) {
        String password = Valid.notBlank(request.getPassword()).trim();
        request.setUserId(Objects1.def(request.getUserId(), Currents::getUserId));
        request.setPassword(password);
        passportService.resetPassword(request);
        return HttpWrapper.ok();
    }

    @PostMapping("/valid")
    @ApiOperation(value = "检查用户信息")
    public UserInfoVO validToken() {
        return Converts.to(Currents.getUser(), UserInfoVO.class);
    }

}
