package com.example.exam.Service;

import com.example.exam.DTO.SeatDTO;
import com.example.exam.exception.BusinessException;
import com.example.exam.model.Exam;
import com.example.exam.model.Seat;
import com.example.exam.respository.ExamRepository;
import com.example.exam.respository.SeatRepository;
import com.example.exam.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ExamRepository examRepository;

    // 根据房间号、起止时间、日期和科目获取座位列表
    public List<Seat> getSeatsByRoom(String room, LocalTime startTime, LocalTime endTime, LocalDate examDate, String subject) {
        if (room == null || room.isEmpty()) {
            throw new BusinessException(400, "试室号不能为空");
        }
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "起止时间不能为空");
        }
        if (examDate == null) {
            throw new BusinessException(400, "考试日期不能为空");
        }
        
        // 首先尝试根据考试信息查找
        Optional<Exam> examOpt = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startTime, endTime, examDate);
        if (examOpt.isPresent()) {
            // 如果找到考试信息，直接返回该考试的座位
            return seatRepository.findByExam(examOpt.get());
        } else {
            // 如果没有找到考试信息，尝试使用原来的查询方法（向后兼容）
            if (subject == null || subject.isEmpty()) {
                throw new BusinessException(400, "考试科目不能为空");
            }
            return seatRepository.findByExamRoomAndTimeAndDate(room, startTime, endTime, examDate, subject);
        }
    }

    // 手动编排座位
    public void manualSeatArrangement(String room, Map<String, String> seatAssignments) {
        if (room == null || room.isEmpty()) {
            throw new BusinessException(400, "试室号不能为空");
        }
        if (seatAssignments == null || seatAssignments.isEmpty()) {
            throw new BusinessException(400, "座位分配信息不能为空");
        }
        
        for (Map.Entry<String, String> entry : seatAssignments.entrySet()) {
            try {
                // 查找座位
                int seatNumber = Integer.parseInt(entry.getKey());
                Seat seat = seatRepository.findByExamRoomAndSeatNumber(room, seatNumber);
                if (seat != null) {
                    seat.setStudentName(entry.getValue().trim());  // 设置学生姓名
                    seat.setAvailable(entry.getValue().isEmpty()); // 如果姓名为空，设置为可用
                    seatRepository.save(seat);  // 保存座位
                } else {
                    throw new BusinessException(404, "未找到试室 " + room + " 中的座位号 " + seatNumber);
                }
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "座位号格式不正确: " + entry.getKey());
            }
        }
    }

    // 解析Excel文件
    public List<SeatDTO> parseExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "Excel文件不能为空");
        }
        
        try {
            List<SeatDTO> seats = ExcelUtils.importSeatsFromExcel(file);
            if (seats.isEmpty()) {
                throw new BusinessException(400, "Excel文件中没有有效的座位数据");
            }
            return seats;
        } catch (IOException e) {
            throw new BusinessException(500, "Excel文件处理失败: " + e.getMessage());
        } catch (BusinessException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new BusinessException(500, "解析Excel文件失败: " + e.getMessage());
        }
    }

    // 导出座位信息到Excel
    public ByteArrayInputStream exportSeatsToExcel(String room, LocalTime startTime, LocalTime endTime, LocalDate examDate) {
        if (room == null || room.isEmpty()) {
            throw new BusinessException(400, "试室号不能为空");
        }
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "起止时间不能为空");
        }
        if (examDate == null) {
            throw new BusinessException(400, "考试日期不能为空");
        }
        
        // 获取考试信息
        Optional<Exam> examOpt = examRepository.findByRoomAndStartTimeAndEndTimeAndExamDate(room, startTime, endTime, examDate);
        if (!examOpt.isPresent()) {
            throw new BusinessException(404, "未找到对应的考试信息");
        }
        
        Exam exam = examOpt.get();
        
        // 获取座位信息
        List<Seat> seats = seatRepository.findByExam(exam);
        if (seats.isEmpty()) {
            throw new BusinessException(404, "该考试没有座位信息");
        }
        
        try {
            return ExcelUtils.exportSeatsToExcel(seats, exam);
        } catch (IOException e) {
            throw new BusinessException(500, "导出Excel文件失败: " + e.getMessage());
        }
    }

    // 保存单个座位信息
    public void saveSeat(Seat seat) {
        if (seat == null) {
            throw new BusinessException(400, "座位信息不能为空");
        }
        if (seat.getSeatNumber() == null) {
            throw new BusinessException(400, "座位号不能为空");
        }
        if (seat.getExam() == null) {
            throw new BusinessException(400, "座位必须关联到考试信息");
        }
        try {
            seatRepository.save(seat);
        } catch (Exception e) {
            throw new BusinessException(500, "保存座位信息失败: " + e.getMessage());
        }
    }
}