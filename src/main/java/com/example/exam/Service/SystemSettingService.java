package com.example.exam.Service;

import com.example.exam.exception.BusinessException;
import com.example.exam.model.SystemSetting;
import com.example.exam.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统设置服务类
 */
@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    /**
     * 获取所有系统设置
     * @return 系统设置列表
     */
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    /**
     * 获取所有系统设置作为Map
     * @return 系统设置Map
     */
    public Map<String, String> getAllSettingsAsMap() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        Map<String, String> settingsMap = new HashMap<>();
        
        for (SystemSetting setting : settings) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        
        return settingsMap;
    }

    /**
     * 根据键获取设置值
     * @param key 设置键
     * @return 设置值
     */
    public String getSettingValue(String key) {
        return getSettingValue(key, null);
    }

    /**
     * 根据键获取设置值，如果不存在则返回默认值
     * @param key 设置键
     * @param defaultValue 默认值
     * @return 设置值或默认值
     */
    public String getSettingValue(String key, String defaultValue) {
        Optional<SystemSetting> setting = systemSettingRepository.findByKey(key);
        return setting.map(SystemSetting::getValue).orElse(defaultValue);
    }

    /**
     * 保存或更新设置
     * @param key 设置键
     * @param value 设置值
     * @param description 设置描述
     * @return 保存的设置
     */
    @Transactional
    public SystemSetting saveOrUpdateSetting(String key, String value, String description) {
        if (key == null || key.trim().isEmpty()) {
            throw new BusinessException(400, "设置键不能为空");
        }
        
        try {
            Optional<SystemSetting> existingSetting = systemSettingRepository.findByKey(key);
            
            if (existingSetting.isPresent()) {
                SystemSetting setting = existingSetting.get();
                setting.setValue(value);
                if (description != null && !description.isEmpty()) {
                    setting.setDescription(description);
                }
                return systemSettingRepository.save(setting);
            } else {
                SystemSetting newSetting = new SystemSetting(key, value, description);
                return systemSettingRepository.save(newSetting);
            }
        } catch (Exception e) {
            throw new BusinessException(500, "保存系统设置失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新设置
     * @param key 设置键
     * @param value 设置值
     * @return 保存的设置
     */
    @Transactional
    public SystemSetting saveOrUpdateSetting(String key, String value) {
        return saveOrUpdateSetting(key, value, null);
    }

    /**
     * 删除设置
     * @param key 设置键
     */
    @Transactional
    public void deleteSetting(String key) {
        systemSettingRepository.deleteByKey(key);
    }

    /**
     * 初始化默认系统设置
     */
    @Transactional
    public void initializeDefaultSettings() {
        // 系统名称
        if (!systemSettingRepository.existsByKey("system.name")) {
            saveOrUpdateSetting("system.name", "考试座位管理系统", "系统名称");
        }
        
        // 每页显示记录数
        if (!systemSettingRepository.existsByKey("system.page.size")) {
            saveOrUpdateSetting("system.page.size", "10", "每页显示记录数");
        }
        
        // 是否允许用户注册
        if (!systemSettingRepository.existsByKey("system.allow.registration")) {
            saveOrUpdateSetting("system.allow.registration", "true", "是否允许用户注册");
        }
        
        // 默认考试开始时间
        if (!systemSettingRepository.existsByKey("exam.default.start.time")) {
            saveOrUpdateSetting("exam.default.start.time", "09:00", "默认考试开始时间");
        }
        
        // 默认考试结束时间
        if (!systemSettingRepository.existsByKey("exam.default.end.time")) {
            saveOrUpdateSetting("exam.default.end.time", "11:00", "默认考试结束时间");
        }
        
        // 默认考试时长（分钟）
        if (!systemSettingRepository.existsByKey("exam.default.duration")) {
            saveOrUpdateSetting("exam.default.duration", "120", "默认考试时长（分钟）");
        }
    }

    /**
     * 获取系统名称
     * @return 系统名称
     */
    public String getSystemName() {
        return getSettingValue("system.name", "考试座位管理系统");
    }

    /**
     * 获取每页显示记录数
     * @return 每页显示记录数
     */
    public int getPageSize() {
        String pageSizeStr = getSettingValue("system.page.size", "10");
        try {
            return Integer.parseInt(pageSizeStr);
        } catch (NumberFormatException e) {
            return 10; // 默认值
        }
    }

    /**
     * 是否允许用户注册
     * @return 是否允许用户注册
     */
    public boolean isAllowRegistration() {
        String allowRegistrationStr = getSettingValue("system.allow.registration", "true");
        return Boolean.parseBoolean(allowRegistrationStr);
    }

    /**
     * 获取默认考试开始时间
     * @return 默认考试开始时间
     */
    public String getDefaultExamStartTime() {
        return getSettingValue("exam.default.start.time", "09:00");
    }

    /**
     * 获取默认考试结束时间
     * @return 默认考试结束时间
     */
    public String getDefaultExamEndTime() {
        return getSettingValue("exam.default.end.time", "11:00");
    }

    /**
     * 获取默认考试时长（分钟）
     * @return 默认考试时长
     */
    public int getDefaultExamDuration() {
        String durationStr = getSettingValue("exam.default.duration", "120");
        try {
            return Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            return 120; // 默认值
        }
    }
}