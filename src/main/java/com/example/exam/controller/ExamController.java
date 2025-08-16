package com.example.exam.controller;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.exam.DTO.ExamSeatDTO;
import com.example.exam.DTO.SeatDTO;
import com.example.exam.exception.BusinessException;
import com.example.exam.model.Exam;
import com.example.exam.Service.ExamService;
import com.example.exam.Service.SeatService;
import com.example.exam.model.Seat;
import com.example.exam.respository.ExamRepository;
import com.example.exam.respository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.io.UnsupportedEncodingException;

@Controller
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SeatRepository seatRepository;

    @GetMapping
    public String getExamPage(Model model) {
        return "exam"; // 返回 exam.html
    }

    @GetMapping("/seats")
    public String getSeatsByRoom(
            @RequestParam("room") String room,
            @RequestParam("start_end_time") String startEndTime,
            @RequestParam("subject") String subject,
            @RequestParam("date") String date,
            Model model) {
        // 解析起止时间
        String[] times = startEndTime.split(" ~ ");
        if (times.length != 2) {
            throw new com.example.exam.exception.BusinessException(400, "时间格式不正确！");
        }

        // 解析日期和时间
        LocalDate examDate;
        LocalTime startTime;
        LocalTime endTime;
        try {
            examDate = LocalDate.parse(date);
            startTime = LocalTime.parse(times[0].trim());
            endTime = LocalTime.parse(times[1].trim());
        } catch (DateTimeParseException e) {
            throw new com.example.exam.exception.BusinessException(400, "日期或时间格式不正确！");
        }

        // 调用服务层方法，根据 room、startTime、endTime 和 examDate 获取座位信息
        List<Seat> seats = seatService.getSeatsByRoom(room, startTime, endTime, examDate, subject);

        if (seats.isEmpty()) {
            model.addAttribute("message", "该试室在指定时间内暂无考生信息！");
        } else {
            model.addAttribute("seats", seats);
        }

        model.addAttribute("room", room);
        model.addAttribute("startEndTime", startEndTime);
        model.addAttribute("subject", subject);
        model.addAttribute("examDate", examDate);

        // 返回 seats.html 页面
        return "seats";
    }

    /*@PostMapping("/save")
    public String saveExam(@RequestParam("subject") String subject,
                           @RequestParam("room") String room,
                           @RequestParam("examType") String examType,
                           @RequestParam("defaultTime") String defaultTime,
                           @RequestParam(value = "customTime", required = false) String customTime,
                           @RequestParam(value = "examDate", required = false) String examDate,
                           @RequestParam(value = "notes", required = false) String notes) {
        String timeRange = (customTime != null && !customTime.isEmpty()) ? customTime : defaultTime;
        String[] times = timeRange.split(" ~ ");
        if (times.length == 2 && examDate != null && !examDate.isEmpty()) {
            try {
                // 解析时间段
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime startTime = LocalTime.parse(times[0].trim(), timeFormatter);
                LocalTime endTime = LocalTime.parse(times[1].trim(), timeFormatter);

                // 解析日期
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate examLocalDate = LocalDate.parse(examDate.trim(), dateFormatter);

                // 构建 Exam 对象
                Exam exam = new Exam();
                exam.setSubject(subject);
                exam.setRoom(room);
                exam.setExamType(examType);
                exam.setStartEndTime(timeRange);
                exam.setStartTime(startTime);
                exam.setEndTime(endTime);
                exam.setExamDate(examLocalDate);
                exam.setNotes(notes);

                // 保存考试信息
                examService.saveExam(exam);
            } catch (DateTimeParseException e) {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }

        return "redirect:/exam";
    }*/
    @PostMapping("/save")
    public String saveExam(@RequestParam("subject") String subject,
                           @RequestParam("room") String room,
                           @RequestParam("examType") String examType,
                           @RequestParam("defaultTime") String defaultTime,
                           @RequestParam(value = "customTime", required = false) String customTime,
                           @RequestParam(value = "examDate", required = false) String examDate,
                           @RequestParam(value = "notes", required = false) String notes) {
        String timeRange = (customTime != null && !customTime.isEmpty()) ? customTime : defaultTime;
        String[] times = timeRange.split(" ~ ");
        if (times.length != 2 || examDate == null || examDate.isEmpty()) {
            throw new com.example.exam.exception.BusinessException(400, "请填写完整的时间范围和日期");
        }
        
        try {
            // 解析时间段
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(times[0].trim(), timeFormatter);
            LocalTime endTime = LocalTime.parse(times[1].trim(), timeFormatter);

            // 解析日期
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate examLocalDate = LocalDate.parse(examDate.trim(), dateFormatter);

            // 检查是否已经存在相同的考试信息
            Optional<Exam> existingExam = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startTime, endTime, examLocalDate);
            if (existingExam.isPresent()) {
                throw new com.example.exam.exception.BusinessException(409, "考试信息已存在");
            }

            // 构建 Exam 对象
            Exam exam = new Exam();
            exam.setSubject(subject);
            exam.setRoom(room);
            exam.setExamType(examType);
            exam.setStartEndTime(timeRange);
            exam.setStartTime(startTime);
            exam.setEndTime(endTime);
            exam.setExamDate(examLocalDate);
            exam.setNotes(notes);

            // 保存考试信息
            examService.saveExam(exam);
        } catch (DateTimeParseException e) {
            throw new com.example.exam.exception.BusinessException(400, "时间或日期格式不正确");
        }

        return "redirect:/exam";
    }

    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> getExamData(
            @RequestParam("room") String room,
            @RequestParam("timeRange") String timeRange,
            @RequestParam("date") String date) { // 新增日期参数
        try {
            // 从 timeRange 拆分起始时间和结束时间
            String[] times = timeRange.split(" ~ ");
            if (times.length != 2) {
                throw new com.example.exam.exception.BusinessException(400, "时间范围格式错误");
            }
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(times[0].trim(), timeFormatter);
            LocalTime endTime = LocalTime.parse(times[1].trim(), timeFormatter);

            // 解析日期
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate examDate = LocalDate.parse(date.trim(), dateFormatter);

            // 从数据库查询对应 room、timeRange 和 date 的考试信息
            ExamSeatDTO examDTO = examService.getExamDetails(room, startTime, endTime, examDate);
            if (examDTO != null) {
                // 返回查询结果
                Map<String, String> response = new HashMap<>();
                response.put("subject", examDTO.getSubject());
                response.put("notes", examDTO.getNotes());
                return ResponseEntity.ok(response);
            } else {
                // 没有找到匹配的考试信息
                throw new com.example.exam.exception.BusinessException(404, "未找到匹配的考试信息");
            }
        } catch (DateTimeParseException e) {
            throw new com.example.exam.exception.BusinessException(400, "时间或日期格式不正确");
        } catch (com.example.exam.exception.BusinessException e) {
            // 业务逻辑异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他未预期的异常
            throw new com.example.exam.exception.BusinessException(500, "服务器内部错误，请稍后再试");
        }
    }


    // 手动编排座位
    @PostMapping("/seats/manual")
    public String manualSeatArrangement(@RequestParam String room, @RequestParam Map<String, String> seatAssignments) {
        seatService.manualSeatArrangement(room, seatAssignments);
        return "redirect:/exam/seats/" + room;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 导出座位信息到Excel
     */
    @GetMapping("/export-seats")
    public ResponseEntity<InputStreamResource> exportSeats(
            @RequestParam("room") String room,
            @RequestParam("timeRange") String timeRange,
            @RequestParam("date") String date) {
        try {
            // 解析时间范围
            String[] times = timeRange.split(" ~ ");
            if (times.length != 2) {
                throw new BusinessException(400, "时间范围格式错误");
            }
            
            // 解析时间
            LocalTime startTime = LocalTime.parse(times[0].trim());
            LocalTime endTime = LocalTime.parse(times[1].trim());
            
            // 解析日期
            LocalDate examDate = LocalDate.parse(date);
            
            // 获取Excel数据
            ByteArrayInputStream in = seatService.exportSeatsToExcel(room, startTime, endTime, examDate);
            
            // 设置HTTP头
            HttpHeaders headers = new HttpHeaders();
            String filename = "座位信息_" + room + "_" + date + ".xlsx";
            headers.add("Content-Disposition", "attachment; filename=" + filename);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(in));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "导出座位信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入座位信息
     */
    @PostMapping("/import-seats")
    public String importSeats(
            @RequestParam("file") MultipartFile file,
            @RequestParam("room") String room,
            @RequestParam("timeRange") String timeRange,
            @RequestParam("date") String date,
            @RequestParam("subject") String subject,
            RedirectAttributes redirectAttributes) {
        try {
            // 解析时间范围
            String[] times = timeRange.split(" ~ ");
            if (times.length != 2) {
                throw new BusinessException(400, "时间范围格式错误");
            }
            
            // 解析时间
            LocalTime startTime = LocalTime.parse(times[0].trim());
            LocalTime endTime = LocalTime.parse(times[1].trim());
            
            // 解析日期
            LocalDate examDate = LocalDate.parse(date);
            
            // 获取或创建考试信息
            Optional<Exam> examOpt = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startTime, endTime, examDate);
            Exam exam;
            
            if (!examOpt.isPresent()) {
                // 如果考试信息不存在，创建新的考试信息
                exam = new Exam();
                exam.setSubject(subject);
                exam.setRoom(room);
                exam.setExamType("闭卷"); // 默认考试类型
                exam.setStartEndTime(timeRange);
                exam.setStartTime(startTime);
                exam.setEndTime(endTime);
                exam.setExamDate(examDate);
                exam.setNotes("通过Excel导入创建");
                
                // 保存考试信息
                exam = examRepository.save(exam);
            } else {
                exam = examOpt.get();
                // 删除该考试的现有座位信息，避免重复
                seatRepository.deleteByExam(exam);
            }
            
            // 解析Excel文件
            List<SeatDTO> seatDTOs = seatService.parseExcel(file);
            
            if (seatDTOs.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Excel文件中没有有效的座位数据");
                return "redirect:/exam";
            }
            
            // 转换为Seat实体并保存
            List<Seat> seats = new ArrayList<>();
            for (SeatDTO dto : seatDTOs) {
                Seat seat = new Seat();
                seat.setSeatNumber(dto.getSeatNumber());
                seat.setStudentName(dto.getStudentName());
                seat.setAvailable(dto.isAvailable());
                seat.setExam(exam);
                seats.add(seat);
            }
            
            // 保存座位信息
            seatRepository.saveAll(seats);
            
            redirectAttributes.addFlashAttribute("success", "成功导入 " + seats.size() + " 条座位信息");
            
            // 使用RedirectAttributes来传递参数，避免URL编码问题
            redirectAttributes.addAttribute("room", room);
            redirectAttributes.addAttribute("start_end_time", timeRange);
            redirectAttributes.addAttribute("subject", subject);
            redirectAttributes.addAttribute("date", date);
            
            return "redirect:/exam/seats";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/exam";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "导入座位信息失败: " + e.getMessage());
            return "redirect:/exam";
        }
    }
}
