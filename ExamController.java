package com.example.exam.controller;
import java.util.Map;
import com.example.exam.model.Exam;
import com.example.exam.Service.ExamService;
import com.example.exam.Service.SeatService;
import com.example.exam.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private SeatService seatService;
    @GetMapping
    public String getExamPage(Model model) {
        List<Exam> exams = examService.getAllExams();  // 获取所有考试
        model.addAttribute("exams", exams);  // 将考试数据添加到模型中
        return "exam";  // 返回考试页面
    }

    @GetMapping("/search")
    public String searchByRoom(@RequestParam("roomNumber") String roomNumber, Model model) {
        // 根据试室号查询考试信息
        Optional<Exam> examOpt = examService.getExamByRoomNumber(roomNumber);
        if (examOpt.isPresent()) {
            Exam exam = examOpt.get();
            List<Seat> seats = seatService.getSeatsByRoom(roomNumber);
            model.addAttribute("exam", exam);
            model.addAttribute("seats", seats);
            return "seats";  // 返回座位表页面
        } else {
            model.addAttribute("message", "未找到该试室的考试信息！");
            return "exam";  // 返回考试输入页面
        }
    }

    @PostMapping("/save")
    public String saveExam(@ModelAttribute Exam exam) {
        examService.saveExam(exam);
        return "redirect:/exam";
    }
    @GetMapping("/seats")
    public String getSeatChart(@RequestParam String room, @RequestParam String subject, Model model) {
        // 查找座位表相关逻辑
        return "seats";  // 返回座位表页面
    }



    // 手动编排座位
    @PostMapping("/seats/manual")
    public String manualSeatArrangement(@RequestParam String room, @RequestParam Map<String, String> seatAssignments) {
        seatService.manualSeatArrangement(room, seatAssignments);
        return "redirect:/exam/seats/" + room;
    }
}
