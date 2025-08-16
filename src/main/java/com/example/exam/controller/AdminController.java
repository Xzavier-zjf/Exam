package com.example.exam.controller;

import com.example.exam.DTO.ExamSeatDTO;
import com.example.exam.DTO.SeatDTO;
import com.example.exam.Service.ExamService;
import com.example.exam.Service.SeatService;
import com.example.exam.Service.SystemSettingService;
import com.example.exam.exception.BusinessException;
import com.example.exam.model.Exam;
import com.example.exam.model.Seat;
import com.example.exam.model.SystemSetting;
import com.example.exam.model.User;
import com.example.exam.respository.ExamRepository;
import com.example.exam.respository.SeatRepository;
import com.example.exam.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import com.example.exam.utils.ExcelUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("systemName", systemSettingService.getSystemName());
                return "admin/user-detail";
            } else {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "获取用户详情失败: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
    
    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, 
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许管理员修改自己的角色
            if (authentication.getName().equals(existingUser.getUsername())) {
                // 保留原有角色
                user.setRoles(existingUser.getRoles());
            }
            
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("message", "用户信息已更新");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新用户信息失败: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, 
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许删除自己的账号
            if (authentication.getName().equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "不能删除当前登录的账号");
                return "redirect:/admin/users";
            }
            
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "用户已删除");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除用户失败: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
    
    /**
     * 更改用户角色
     */
    @PostMapping("/users/{userId}/role")
    public String changeUserRole(@PathVariable Long userId, 
                                @RequestParam String role,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许修改自己的角色
            if (authentication.getName().equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "不能修改自己的角色");
                return "redirect:/admin/users";
            }
            
            userService.setUserRole(user, role);
            redirectAttributes.addFlashAttribute("success", "用户角色已更新为: " + role);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新用户角色失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/users/{userId}/toggle-status")
    public String toggleUserStatus(@PathVariable Long userId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许禁用自己的账号
            if (authentication.getName().equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "不能禁用当前登录的账号");
                return "redirect:/admin/users";
            }
            
            boolean newStatus = !user.isActive();
            if (newStatus) {
                userService.enableUser(user);
                redirectAttributes.addFlashAttribute("success", "用户 " + user.getUsername() + " 已启用");
            } else {
                userService.disableUser(user);
                redirectAttributes.addFlashAttribute("success", "用户 " + user.getUsername() + " 已禁用");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新用户状态失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**
     * 显示系统设置页面
     */
    @GetMapping("/settings")
    public String showSettings(Model model) {
        // 从配置服务中加载系统设置
        Map<String, String> settings = systemSettingService.getAllSettingsAsMap();
        model.addAttribute("settings", settings);
        model.addAttribute("systemName", systemSettingService.getSystemName());
        model.addAttribute("pageSize", systemSettingService.getPageSize());
        model.addAttribute("allowRegistration", systemSettingService.isAllowRegistration());
        model.addAttribute("defaultExamStartTime", systemSettingService.getDefaultExamStartTime());
        model.addAttribute("defaultExamEndTime", systemSettingService.getDefaultExamEndTime());
        model.addAttribute("defaultExamDuration", systemSettingService.getDefaultExamDuration());
        
        return "admin/settings";
    }
    
    /**
     * 更新系统设置
     */
    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        try {
            // 更新系统名称
            if (allParams.containsKey("systemName")) {
                systemSettingService.saveOrUpdateSetting("system.name", allParams.get("systemName"), "系统名称");
            }
            
            // 更新每页显示记录数
            if (allParams.containsKey("pageSize")) {
                systemSettingService.saveOrUpdateSetting("system.page.size", allParams.get("pageSize"), "每页显示记录数");
            }
            
            // 更新是否允许用户注册
            String allowRegistration = allParams.getOrDefault("allowRegistration", "false");
            systemSettingService.saveOrUpdateSetting("system.allow.registration", allowRegistration, "是否允许用户注册");
            
            // 更新默认考试开始时间
            if (allParams.containsKey("defaultExamStartTime")) {
                systemSettingService.saveOrUpdateSetting("exam.default.start.time", allParams.get("defaultExamStartTime"), "默认考试开始时间");
            }
            
            // 更新默认考试结束时间
            if (allParams.containsKey("defaultExamEndTime")) {
                systemSettingService.saveOrUpdateSetting("exam.default.end.time", allParams.get("defaultExamEndTime"), "默认考试结束时间");
            }
            
            // 更新默认考试时长
            if (allParams.containsKey("defaultExamDuration")) {
                systemSettingService.saveOrUpdateSetting("exam.default.duration", allParams.get("defaultExamDuration"), "默认考试时长（分钟）");
            }
            
            redirectAttributes.addFlashAttribute("success", "系统设置已更新");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新系统设置失败: " + e.getMessage());
        }
        
        return "redirect:/admin/settings";
    }
    
    /**
     * 备份系统数据
     */
    @PostMapping("/settings/backup")
    public String backupSystem(RedirectAttributes redirectAttributes) {
        try {
            // 这里实现数据备份逻辑
            // 在实际应用中，可能需要调用专门的备份服务
            
            redirectAttributes.addFlashAttribute("success", "系统数据已备份");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "备份系统数据失败: " + e.getMessage());
        }
        
        return "redirect:/admin/settings";
    }
    
    /**
     * 恢复系统数据
     */
    @PostMapping("/settings/restore")
    public String restoreSystem(RedirectAttributes redirectAttributes) {
        try {
            // 这里实现数据恢复逻辑
            // 在实际应用中，可能需要调用专门的恢复服务
            
            redirectAttributes.addFlashAttribute("success", "系统数据已恢复");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "恢复系统数据失败: " + e.getMessage());
        }
        
        return "redirect:/admin/settings";
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/users/{userId}/reset-password")
    public String resetUserPassword(@PathVariable Long userId, 
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许重置自己的密码（应该使用修改密码功能）
            if (authentication.getName().equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "请使用修改密码功能更改自己的密码");
            } else {
                // 重置密码逻辑
                userService.resetUserPassword(userId);
                redirectAttributes.addFlashAttribute("success", "密码已重置，新密码已发送到用户邮箱");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "重置密码失败: " + e.getMessage());
        }
        return "redirect:/admin/users/" + userId;
    }
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatService seatService;

    @Autowired
    private ExamService examService;
    
    @Autowired
    private SystemSettingService systemSettingService;

    // 显示座位信息 - 获取所有座位

    @GetMapping
    public String seatArrangementPage(Model model) {
        model.addAttribute("room", ""); // 默认传递空字符串
        return "admin"; // 返回 admin 页面
    }

    // 保存座位信息
   /* @PostMapping("/save")
    @Transactional // 确保事务管理
    public String saveSeatArrangement(@RequestBody Map<String, Object> payload) {
        String room = (String) payload.get("room");
        String startTime = (String) payload.get("startTime"); // 获取开始时间
        String endTime = (String) payload.get("endTime"); // 获取结束时间
        String examDateStr = (String) payload.get("examDate"); // 获取考试日期

        // 获取座位数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seatAssignments = (List<Map<String, Object>>) payload.get("seats");

        // 检查房间号和时间参数是否为空
        if (room == null || room.isEmpty()) {
            throw new IllegalArgumentException("房间号不能为空");
        }
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            throw new IllegalArgumentException("起止时间不能为空");
        }
        if (examDateStr == null || examDateStr.isEmpty()) {
            throw new IllegalArgumentException("考试日期不能为空");
        }
        if (seatAssignments == null || seatAssignments.isEmpty()) {
            throw new IllegalArgumentException("座位信息不能为空");
        }

        // 将时间字符串转换为 LocalTime 类型
        LocalTime startLocalTime;
        LocalTime endLocalTime;
        try {
            startLocalTime = LocalTime.parse(startTime);
            endLocalTime = LocalTime.parse(endTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间格式不正确，请使用 HH:mm 格式，例如 08:40");
        }

        // 将考试日期字符串转换为 LocalDate 类型
        LocalDate examDate;
        try {
            examDate = LocalDate.parse(examDateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("考试日期格式不正确，请使用 yyyy-MM-dd 格式，例如 2025-01-01");
        }

        // 从数据库中获取与房间和时间段对应的 Exam 对象（包括日期）
        Exam exam = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startLocalTime, endLocalTime, examDate)
                .orElseThrow(() -> new IllegalArgumentException("找不到对应的考试房间和时间段：" + room + " " + startTime + "~" + endTime + " " + examDate));

        // 转化并保存 Seat 数据
        List<Seat> seats = seatAssignments.stream().map(seatData -> {
            Seat seat = new Seat();
            seat.setSeatNumber((Integer) seatData.get("seatNumber"));
            seat.setStudentName((String) seatData.get("studentName"));
            seat.setAvailable(Boolean.parseBoolean(String.valueOf(seatData.get("available"))));
            seat.setExam(exam); // 设置关联考试
            return seat;
        }).collect(Collectors.toList());

        // 检查座位号是否有重复
        Set<Integer> seatNumbers = new HashSet<>();
        for (Seat seat : seats) {
            if (!seatNumbers.add(seat.getSeatNumber())) {
                throw new IllegalArgumentException("座位号 " + seat.getSeatNumber() + " 重复，请修改后再保存！");
            }
        }

        // 检查数据库中是否已有重复座位号
        List<Seat> existingSeats = seatRepository.findByExam(exam);
        for (Seat seat : seats) {
            for (Seat existingSeat : existingSeats) {
                if (seat.getSeatNumber().equals(existingSeat.getSeatNumber())) {
                    throw new IllegalArgumentException("座位号 " + seat.getSeatNumber() + " 已存在于该房间中，请修改后再保存！");
                }
            }
        }

        // 批量保存座位数据
        seatRepository.saveAll(seats);

        return "redirect:/admin?room=" + room;
    }*/
    @PostMapping("/save")
    @Transactional // 确保事务管理
    public String saveSeatArrangement(@RequestBody Map<String, Object> payload) {
        String room = (String) payload.get("room");
        String startTime = (String) payload.get("startTime"); // 获取开始时间
        String endTime = (String) payload.get("endTime"); // 获取结束时间
        String examDateStr = (String) payload.get("examDate"); // 获取考试日期

        // 获取座位数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seatAssignments = (List<Map<String, Object>>) payload.get("seats");

        // 检查房间号和时间参数是否为空
        if (room == null || room.isEmpty()) {
            throw new BusinessException(400, "房间号不能为空");
        }
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            throw new BusinessException(400, "起止时间不能为空");
        }
        if (examDateStr == null || examDateStr.isEmpty()) {
            throw new BusinessException(400, "考试日期不能为空");
        }
        if (seatAssignments == null || seatAssignments.isEmpty()) {
            throw new BusinessException(400, "座位信息不能为空");
        }

        // 将时间字符串转换为 LocalTime 类型
        LocalTime startLocalTime;
        LocalTime endLocalTime;
        try {
            startLocalTime = LocalTime.parse(startTime);
            endLocalTime = LocalTime.parse(endTime);
        } catch (DateTimeParseException e) {
            throw new BusinessException(400, "时间格式不正确，请使用 HH:mm 格式，例如 08:40");
        }

        // 将考试日期字符串转换为 LocalDate 类型
        LocalDate examDate;
        try {
            examDate = LocalDate.parse(examDateStr);
        } catch (DateTimeParseException e) {
            throw new BusinessException(400, "考试日期格式不正确，请使用 yyyy-MM-dd 格式，例如 2025-01-01");
        }

        // 从数据库中获取与房间和时间段对应的 Exam 对象（包括日期）
        Exam exam = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startLocalTime, endLocalTime, examDate)
                .orElseThrow(() -> new BusinessException(404, "找不到对应的考试房间和时间段：" + room + " " + startTime + "~" + endTime + " " + examDate));

        // 转化并保存 Seat 数据
        List<Seat> seats = seatAssignments.stream().map(seatData -> {
            Seat seat = new Seat();
            seat.setSeatNumber((Integer) seatData.get("seatNumber"));
            seat.setStudentName((String) seatData.get("studentName"));
            seat.setAvailable(Boolean.parseBoolean(String.valueOf(seatData.get("available"))));
            seat.setExam(exam); // 设置关联考试
            return seat;
        }).collect(Collectors.toList());

        // 检查座位号是否有重复
        Set<Integer> seatNumbers = new HashSet<>();
        for (Seat seat : seats) {
            if (!seatNumbers.add(seat.getSeatNumber())) {
                throw new BusinessException(400, "座位号 " + seat.getSeatNumber() + " 重复，请修改后再保存！");
            }
        }

        // 检查数据库中是否已有重复座位号
        List<Seat> existingSeats = seatRepository.findByExam(exam);
        for (Seat seat : seats) {
            for (Seat existingSeat : existingSeats) {
                if (seat.getSeatNumber().equals(existingSeat.getSeatNumber())) {
                    throw new BusinessException(400, "座位号 " + seat.getSeatNumber() + " 已存在于该房间中，请修改后再保存！");
                }
            }
        }

        // 批量保存座位数据
        try {
            seatRepository.saveAll(seats);
        } catch (Exception e) {
            throw new BusinessException(500, "保存座位信息失败: " + e.getMessage());
        }

        return "redirect:/admin?room=" + room;
    }



    @PostMapping("/upload-seat-chart")
    @ResponseBody
    public ResponseEntity<String> uploadSeatChart(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("room") String room,
                                                  @RequestParam("startTime") String startTime,
                                                  @RequestParam("endTime") String endTime,
                                                  @RequestParam("examDate") String examDate) {
        try {
            // 检查参数
            if (room == null || room.isEmpty()) {
                throw new BusinessException(400, "房间号不能为空");
            }
            if (startTime == null || startTime.isEmpty()) {
                throw new BusinessException(400, "开始时间不能为空");
            }
            if (endTime == null || endTime.isEmpty()) {
                throw new BusinessException(400, "结束时间不能为空");
            }
            if (examDate == null || examDate.isEmpty()) {
                throw new BusinessException(400, "考试日期不能为空");
            }
            
            // 解析 Excel 文件
            List<SeatDTO> seatData = seatService.parseExcel(file);

            // 将时间字符串转换为 LocalTime 类型
            LocalTime startLocalTime;
            LocalTime endLocalTime;
            try {
                startLocalTime = LocalTime.parse(startTime);
                endLocalTime = LocalTime.parse(endTime);
            } catch (DateTimeParseException e) {
                throw new BusinessException(400, "时间格式不正确，请使用 HH:mm 格式，例如 08:40");
            }

            // 将考试日期字符串转换为 LocalDate 类型
            LocalDate examLocalDate;
            try {
                examLocalDate = LocalDate.parse(examDate);
            } catch (DateTimeParseException e) {
                throw new BusinessException(400, "考试日期格式不正确，请使用 yyyy-MM-dd 格式，例如 2025-01-01");
            }

            // 从数据库中获取与房间和时间段对应的 Exam 对象
            Exam exam = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startLocalTime, endLocalTime, examLocalDate)
                    .orElseThrow(() -> new BusinessException(404, "找不到对应的考试房间和时间段：" + room + " " + startTime + "~" + endTime + " " + examDate));

            // 将 SeatDTO 转换为 Seat 实体并保存到数据库
            List<Seat> seats = seatData.stream().map(seatDTO -> {
                Seat seat = new Seat();
                seat.setSeatNumber(seatDTO.getSeatNumber());
                seat.setStudentName(seatDTO.getStudentName());
                seat.setAvailable(seatDTO.getStudentName() == null || seatDTO.getStudentName().isEmpty());
                seat.setExam(exam); // 确保关联到正确的考试
                return seat;
            }).collect(Collectors.toList());

            // 检查座位号是否有重复
            Set<Integer> seatNumbers = new HashSet<>();
            for (Seat seat : seats) {
                if (!seatNumbers.add(seat.getSeatNumber())) {
                    throw new BusinessException(400, "导入的Excel中座位号 " + seat.getSeatNumber() + " 重复，请修改后再导入！");
                }
            }

            // 检查座位号和学生姓名是否重复
            List<Seat> existingSeats = seatRepository.findByExam(exam);
            for (Seat seat : seats) {
                for (Seat existingSeat : existingSeats) {
                    if (seat.getSeatNumber().equals(existingSeat.getSeatNumber())) {
                        throw new BusinessException(400, "座位号 " + seat.getSeatNumber() + " 已存在于该房间中，请修改后再导入！");
                    }
                }
            }

            // 批量保存座位数据
            try {
                seatRepository.saveAll(seats);
                return ResponseEntity.ok("座位信息导入成功！");
            } catch (Exception e) {
                throw new BusinessException(500, "保存座位信息失败: " + e.getMessage());
            }
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("座位信息导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/exam/data")
    @ResponseBody
    public ExamSeatDTO getExamData(@RequestParam String room,
                                   @RequestParam String timeRange,
                                   @RequestParam String date) {
        try {
            // 解析时间范围
            String[] times = timeRange.split(" ~ ");
            LocalTime startTime = LocalTime.parse(times[0]);
            LocalTime endTime = LocalTime.parse(times[1]);

            // 解析日期
            LocalDate examDate = LocalDate.parse(date);

            // 获取考试数据
            return examService.getExamDetails(room, startTime, endTime, examDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("时间格式不正确，请使用 HH:mm ~ HH:mm 格式，例如 08:40 ~ 10:40");
        }
    }

}
