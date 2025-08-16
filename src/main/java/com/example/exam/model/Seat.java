package com.example.exam.model;

import jakarta.persistence.*;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 座位ID

    private Integer seatNumber;    // 座位编号
    private String studentName;    // 学生姓名
    private Boolean available;     // 座位是否可用

    @ManyToOne
    @JoinColumn(name = "exam_id" ,nullable = false)  // 外键关联到Exam表
    private Exam exam;             // 该座位属于哪个考试

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
    
    public boolean isAvailable() {
        return available != null ? available : true;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}
