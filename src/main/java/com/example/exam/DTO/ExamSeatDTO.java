package com.example.exam.DTO;




public class ExamSeatDTO {
    private String subject;
    private String notes;


    public ExamSeatDTO(String subject, String notes) {
        this.subject = subject;
        this.notes = notes;

    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }}



