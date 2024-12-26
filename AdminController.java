package com.example.exam.controller;

import com.example.exam.Service.SeatService;
import com.example.exam.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SeatService seatService;

    // 显示座位信息 - 获取所有座位
    @GetMapping
    public String seatArrangementPage(@RequestParam String room, Model model) {
        // 根据房间号获取座位信息
        List<Seat> seats = seatService.getSeatsByRoom(room);
        model.addAttribute("seats", seats);
        model.addAttribute("room", room);  // 将房间信息传递给前端
        return "admin";  // 返回 admin 页面
    }

    // 保存座位信息
    @PostMapping("/save")
    public String saveSeatArrangement(@RequestParam String room, @RequestBody List<Map<String, String>> seatAssignments) {
        // 获取当前房间的座位
        List<Seat> seats = seatService.getSeatsByRoom(room);

        // 更新座位信息
        for (Seat seat : seats) {
            for (Map<String, String> assignment : seatAssignments) {
                Integer seatNumber = Integer.parseInt(assignment.get("seatNumber"));
                String studentName = assignment.get("studentName");

                if (seat.getSeatNumber().equals(seatNumber)) {
                    if (studentName != null && !studentName.trim().isEmpty()) {
                        seat.setStudentName(studentName.trim());  // 设置学生姓名
                        seat.setAvailable(false);  // 座位已占用
                    } else {
                        seat.setStudentName(null);  // 清空学生姓名
                        seat.setAvailable(true);    // 座位可用
                    }
                    seatService.saveSeat(seat);  // 保存更新的座位信息
                }
            }
        }

        // 重定向到当前房间的页面
        return "redirect:/admin?room=" + room;
    }

}
