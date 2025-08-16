package com.example.exam.repository;

import com.example.exam.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统设置数据访问接口
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * 根据设置键查找设置
     * @param key 设置键
     * @return 设置对象
     */
    Optional<SystemSetting> findByKey(String key);
    
    /**
     * 检查设置键是否存在
     * @param key 设置键
     * @return 是否存在
     */
    boolean existsByKey(String key);
    
    /**
     * 根据设置键删除设置
     * @param key 设置键
     */
    void deleteByKey(String key);
}