package com.example.exam.controller;

import com.example.exam.Service.UserService;
import com.example.exam.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 显示登录页面
    @GetMapping("/login")
    public String showLoginPage(Model model, 
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout,
                              @RequestParam(value = "expired", required = false) String expired) {
        // 如果用户已经登录，重定向到首页
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        
        if (logout != null) {
            model.addAttribute("message", "您已成功退出登录");
        }
        
        if (expired != null) {
            model.addAttribute("error", "会话已过期，请重新登录");
        }
        
        return "login";
    }

    // 显示注册页面
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // 处理注册请求
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String fullName,
                           Model model) {
        // 验证密码
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "register";
        }
        
        // 验证密码强度
        if (password.length() < 6) {
            model.addAttribute("error", "密码长度不能少于6个字符");
            return "register";
        }
        
        try {
            userService.registerUser(username, password, email, fullName);
            model.addAttribute("success", "注册成功，请登录");
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    // 处理登出请求
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/auth/login?logout=true";
    }
    
    // 显示修改密码页面 - 重定向到新的修改密码页面
    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "redirect:/users/change-password";
    }
    
    // 处理修改密码请求 - 重定向到新的修改密码处理方法
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        // 将请求重定向到UserController中的修改密码方法
        redirectAttributes.addAttribute("currentPassword", currentPassword);
        redirectAttributes.addAttribute("newPassword", newPassword);
        redirectAttributes.addAttribute("confirmPassword", confirmPassword);
        return "redirect:/users/change-password";
    }
}