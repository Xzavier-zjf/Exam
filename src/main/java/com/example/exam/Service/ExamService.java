package com.example.exam.Service;

import com.example.exam.DTO.ExamSeatDTO;
import com.example.exam.exception.BusinessException;
import com.example.exam.model.Exam;
import com.example.exam.respository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    public Optional<Exam> getExamByRoomNumber(String roomNumber) {
        return examRepository.findByRoom(roomNumber);
    }

    public void saveExam(Exam exam) {
        if (exam == null) {
            throw new BusinessException(400, "考试信息不能为空");
        }
        
        if (exam.getRoom() == null || exam.getRoom().trim().isEmpty()) {
            throw new BusinessException(400, "考试房间不能为空");
        }
        
        if (exam.getSubject() == null || exam.getSubject().trim().isEmpty()) {
            throw new BusinessException(400, "考试科目不能为空");
        }
        
        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            throw new BusinessException(400, "考试时间不能为空");
        }
        
        if (exam.getExamDate() == null) {
            throw new BusinessException(400, "考试日期不能为空");
        }
        
        try {
            examRepository.save(exam);
        } catch (Exception e) {
            throw new BusinessException(500, "保存考试信息失败: " + e.getMessage());
        }
    }
   // @Override

    public ExamSeatDTO getExamDetails(String room, LocalTime startTime, LocalTime endTime, LocalDate examDate) {
        if (room == null || room.trim().isEmpty()) {
            throw new BusinessException(400, "房间号不能为空");
        }
        
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "考试时间不能为空");
        }
        
        if (examDate == null) {
            throw new BusinessException(400, "考试日期不能为空");
        }
        
        ExamSeatDTO examDetails = examRepository.findExamDetailsByRoomAndTimeAndDate(room, startTime, endTime, examDate);
        if (examDetails == null) {
            throw new BusinessException(404, "未找到指定房间和时间的考试信息");
        }
        
        return examDetails;
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();  // 获取所有考试记录
    }

}
