package com.example.exam.respository;

import com.example.exam.model.Exam;
import com.example.exam.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.exam.room = :room AND s.exam.startTime = :startTime AND s.exam.endTime = :endTime AND s.exam.examDate = :examDate AND s.exam.subject = :subject")
    List<Seat> findByExamRoomAndTimeAndDate(@Param("room") String room,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime,
                                            @Param("examDate") LocalDate examDate,
                                            @Param("subject") String subject);

    @Query("SELECT s FROM Seat s WHERE s.exam.room = :room AND s.exam.startTime = :startTime AND s.exam.endTime = :endTime AND s.exam.examDate = :examDate ORDER BY s.seatNumber")
    List<Seat> findByExamRoomAndTimeAndDateOnly(@Param("room") String room,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime,
                                                @Param("examDate") LocalDate examDate);

    Seat findByExamRoomAndSeatNumber(String room, Integer seatNumber);
    List<Seat> findByExam(Exam exam);
    List<Seat> findByExam_Room(String room);
    
    @Modifying
    @Transactional
    void deleteByExam(Exam exam);
}
