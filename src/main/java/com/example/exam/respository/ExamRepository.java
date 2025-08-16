package com.example.exam.respository;



import com.example.exam.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.exam.DTO.ExamSeatDTO;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByRoom(String room);   // 根据试室号查找考试
   //Optional<Exam> findByRoomAndStartEndTime(String room, String startEndTime);
   Optional<Exam> findByRoomAndStartTimeAndEndTimeAndExamDate(String room, LocalTime startTime, LocalTime endTime, LocalDate examDate);

    @Query("SELECT new com.example.exam.DTO.ExamSeatDTO(e.subject, e.notes) " +
            "FROM Exam e WHERE e.room = :room AND e.examDate = :examDate " +
            "AND e.startTime = :startTime AND e.endTime = :endTime")
    ExamSeatDTO findExamDetailsByRoomAndTimeAndDate(@Param("room") String room,
                                                    @Param("startTime") LocalTime startTime,
                                                    @Param("endTime") LocalTime endTime,
                                                    @Param("examDate") LocalDate examDate);


}
