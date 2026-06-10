package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.LoginDevice;
import com.blog.vo.LoginDeviceVO;

import java.util.List;

public interface LoginDeviceService extends IService<LoginDevice> {

    /**
     * 记录登录设备
     */
    void recordLogin(Long userId, String ip, String userAgent);

    /**
     * 获取用户的登录设备列表
     */
    List<LoginDeviceVO> getDevices(Long userId, String currentTokenIp);

    /**
     * 踢出指定设备
     */
    void logoutDevice(Long deviceId, Long userId);

    /**
     * 删除设备记录
     */
    void deleteDevice(Long deviceId, Long userId);
}
