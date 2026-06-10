package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.LoginDeviceService;
import com.blog.vo.LoginDeviceVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final LoginDeviceService loginDeviceService;

    public DeviceController(LoginDeviceService loginDeviceService) {
        this.loginDeviceService = loginDeviceService;
    }

    @GetMapping
    public Result<List<LoginDeviceVO>> getDevices(HttpServletRequest request) {
        Long userId = getCurrentUserId();
        String clientIp = getClientIp(request);
        return Result.success(loginDeviceService.getDevices(userId, clientIp));
    }

    @DeleteMapping("/{id}")
    public Result<Void> logoutDevice(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        loginDeviceService.logoutDevice(id, userId);
        return Result.success();
    }

    @DeleteMapping("/record/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        loginDeviceService.deleteDevice(id, userId);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getHeader("X-Real-IP");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        // IPv6 本地回环转 IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}
