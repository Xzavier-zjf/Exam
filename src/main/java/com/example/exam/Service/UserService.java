package com.example.exam.Service;

import com.example.exam.model.User;
import com.example.exam.respository.UserRepository;
import com.example.exam.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 实现UserDetailsService接口的方法，用于Spring Security认证
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        if (!user.isActive()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 简化角色处理，直接使用用户角色
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    // 用户登录
    @Transactional
    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 更新最后登录时间
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return user;
        }
        return null;
    }

    // 用户注册
    @Transactional
    public User registerUser(String username, String password, String email, String fullName) throws Exception {
        // 验证用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(400, "用户名已存在");
        }
        
        // 验证邮箱是否已存在
        if (email != null && !email.isEmpty() && userRepository.findByEmail(email).isPresent()) {
            throw new Exception("邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole("ROLE_USER"); // 默认角色
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    // 简化的注册方法，兼容旧代码
    @Transactional
    public User registerUser(String username, String password) throws Exception {
        return registerUser(username, password, null, null);
    }

    // 获取用户信息
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // 更新用户信息
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    // 更改用户密码
    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    
    // 设置用户角色
    @Transactional
    public void setUserRole(String username, String role) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        user.setRole(role);
        userRepository.save(user);
    }
    
    // 检查用户是否是管理员
    public boolean isAdmin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(User::isAdmin).orElse(false);
    }
    
    // 禁用/启用用户
    @Transactional
    public void setUserActiveStatus(String username, boolean isActive) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        user.setActive(isActive);
        userRepository.save(user);
    }
    
    // 启用用户
    @Transactional
    public void enableUser(User user) throws Exception {
        setUserActiveStatus(user.getUsername(), true);
    }

    // 禁用用户
    @Transactional
    public void disableUser(User user) throws Exception {
        setUserActiveStatus(user.getUsername(), false);
    }

    // 根据ID获取用户
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    // 更新用户个人资料
    @Transactional
    public void updateUserProfile(String username, String fullName, String email) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        user.setFullName(fullName);
        user.setEmail(email);
        userRepository.save(user);
    }

    // 更新用户信息（根据ID）
    @Transactional
    public User updateUser(Long id, User updatedUser) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        // 更新用户信息，但不更新密码和角色
        if (updatedUser.getFullName() != null) {
            user.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.isActive() != user.isActive()) {
            user.setActive(updatedUser.isActive());
        }
        
        return userRepository.save(user);
    }
    
    // 删除用户
    @Transactional
    public void deleteUser(Long id) throws Exception {
        if (!userRepository.existsById(id)) {
            throw new Exception("用户不存在");
        }
        userRepository.deleteById(id);
    }
    
    // 切换用户状态
    @Transactional
    public void toggleUserStatus(Long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    // 设置用户状态（根据ID）
    @Transactional
    public void setUserStatus(Long id, boolean isActive) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        user.setActive(isActive);
        userRepository.save(user);
    }
    
    // 设置用户角色（根据ID）
    @Transactional
    public void setUserRole(Long id, String role) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setRole(role);
        userRepository.save(user);
    }
    
    // 设置用户角色（根据用户对象）
    @Transactional
    public void setUserRole(User user, String role) throws Exception {
        user.setRole(role);
        userRepository.save(user);
    }
    
    // 重置用户密码
    @Transactional
    public String resetPassword(Long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        // 生成随机密码
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 返回新密码（在实际应用中，应该发送邮件而不是直接返回）
        return newPassword;
    }
    
    // 生成随机密码
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 更新用户信息（不包括密码和角色）
    @Transactional
    public User updateUserInfo(User user) {
        return userRepository.save(user);
    }
    
    // 重置用户密码（用于管理员界面）
    @Transactional
    public String resetUserPassword(Long userId) throws Exception {
        return resetPassword(userId);
    }
}