package com.example.exam.controller;

import com.example.exam.Service.SystemSettingService;
import com.example.exam.Service.UserService;
import com.example.exam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * 显示用户个人资料页面
     */
    @GetMapping("/profile")
    public String showUserProfile(Model model) {
        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "admin"; // 默认使用admin用户
        
        // 如果是匿名用户，使用默认用户
        if ("anonymousUser".equals(username)) {
            username = "admin";
        }
        
        User user = userService.getUserByUsername(username).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("systemName", systemSettingService.getSystemName());
            return "profile";
        } else {
            // 如果找不到用户，创建一个默认用户信息
            User defaultUser = new User();
            defaultUser.setUsername("admin");
            defaultUser.setFullName("系统管理员");
            defaultUser.setEmail("admin@exam.com");
            defaultUser.setRole("ROLE_ADMIN");
            model.addAttribute("user", defaultUser);
            model.addAttribute("systemName", systemSettingService.getSystemName());
            return "profile";
        }
    }
    
    /**
     * 处理更新用户个人资料请求
     */
    @PostMapping("/profile/update")
    public String updateUserProfile(@RequestParam String fullName,
                                   @RequestParam String email,
                                   RedirectAttributes redirectAttributes) {
        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "admin"; // 默认使用admin用户
        
        // 如果是匿名用户，使用默认用户
        if ("anonymousUser".equals(username)) {
            username = "admin";
        }
        
        try {
            userService.updateUserProfile(username, fullName, email);
            redirectAttributes.addFlashAttribute("success", "个人资料更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        
        return "redirect:/users/profile";
    }
    
    /**
     * 显示修改密码页面
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("systemName", systemSettingService.getSystemName());
        return "change-password";
    }
    
    /**
     * 处理修改密码请求
     * 支持直接提交和从AuthController重定向过来的请求
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "admin"; // 默认使用admin用户
        
        // 如果是匿名用户，使用默认用户
        if ("anonymousUser".equals(username)) {
            username = "admin";
        }
        
        // 验证新密码长度
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "新密码长度不能少于6个字符");
            return "redirect:/users/change-password";
        }
        
        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "新密码和确认密码不一致");
            return "redirect:/users/change-password";
        }
        
        try {
            boolean success = userService.changePassword(username, currentPassword, newPassword);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "密码修改成功");
                return "redirect:/users/profile";
            } else {
                redirectAttributes.addFlashAttribute("error", "当前密码不正确");
                return "redirect:/users/change-password";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "密码修改失败: " + e.getMessage());
            return "redirect:/users/change-password";
        }
    }
    
    /**
     * 显示用户列表（仅管理员可访问）
     */
    @GetMapping("/list")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("systemName", systemSettingService.getSystemName());
        return "user-list";
    }
    
    /**
     * 启用/禁用用户（仅管理员可访问）
     */
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("success", "用户状态已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        
        return "redirect:/users/list";
    }
    
    /**
     * 更改用户角色（仅管理员可访问）
     */
    @PostMapping("/change-role/{id}")
    public String changeUserRole(@PathVariable Long id, 
                               @RequestParam String role,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.setUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "用户角色已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        
        return "redirect:/users/list";
    }
}