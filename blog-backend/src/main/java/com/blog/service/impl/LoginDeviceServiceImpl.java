package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.LoginDevice;
import com.blog.exception.BusinessException;
import com.blog.mapper.LoginDeviceMapper;
import com.blog.service.LoginDeviceService;
import com.blog.util.UserAgentParser;
import com.blog.vo.LoginDeviceVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoginDeviceServiceImpl extends ServiceImpl<LoginDeviceMapper, LoginDevice>
        implements LoginDeviceService {

    @Override
    public void recordLogin(Long userId, String ip, String userAgent) {
        UserAgentParser.DeviceInfo deviceInfo = UserAgentParser.parse(userAgent);

        LoginDevice device = new LoginDevice();
        device.setUserId(userId);
        device.setIp(ip);
        device.setDeviceType(deviceInfo.getDeviceType());
        device.setBrowser(deviceInfo.getBrowser());
        device.setOs(deviceInfo.getOs());
        device.setUserAgent(userAgent);
        device.setStatus(1);
        device.setLastActiveTime(LocalDateTime.now());
        save(device);
    }

    @Override
    public List<LoginDeviceVO> getDevices(Long userId, String currentIp) {
        LambdaQueryWrapper<LoginDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginDevice::getUserId, userId)
               .orderByDesc(LoginDevice::getLastActiveTime);
        List<LoginDevice> devices = list(wrapper);

        // 按设备名（os + browser）去重，只保留最新的记录
        Map<String, LoginDevice> deviceMap = new LinkedHashMap<>();
        for (LoginDevice d : devices) {
            String key = d.getOs() + "|" + d.getBrowser();
            // 只保留第一条（最新的，因为已按 lastActiveTime 降序排列）
            deviceMap.putIfAbsent(key, d);
        }

        return deviceMap.values().stream().map(d -> {
            LoginDeviceVO vo = new LoginDeviceVO();
            vo.setId(d.getId());
            vo.setIp(d.getIp());
            vo.setDeviceType(d.getDeviceType());
            vo.setBrowser(d.getBrowser());
            vo.setOs(d.getOs());
            vo.setStatus(d.getStatus());
            vo.setLastActiveTime(d.getLastActiveTime());
            vo.setCreateTime(d.getCreateTime());
            // 标记当前设备：IP 相同且状态在线
            vo.setCurrent(d.getStatus() == 1 && d.getIp().equals(currentIp));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void logoutDevice(Long deviceId, Long userId) {
        LoginDevice device = getById(deviceId);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BusinessException("设备不存在");
        }
        if (device.getStatus() == 0) {
            throw new BusinessException("该设备已下线");
        }
        device.setStatus(0);
        updateById(device);
    }

    @Override
    public void deleteDevice(Long deviceId, Long userId) {
        LoginDevice device = getById(deviceId);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BusinessException("设备不存在");
        }
        removeById(deviceId);
    }
}
