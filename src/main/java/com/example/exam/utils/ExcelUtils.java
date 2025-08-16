package com.example.exam.utils;

import com.example.exam.DTO.SeatDTO;
import com.example.exam.model.Exam;
import com.example.exam.model.Seat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {

    /**
     * 从Excel文件导入座位数据
     * @param file Excel文件
     * @return 座位数据列表
     */
    public static List<SeatDTO> importSeatsFromExcel(MultipartFile file) throws IOException {
        List<SeatDTO> seats = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            
            // 跳过标题行
            if (rows.hasNext()) {
                rows.next();
            }
            
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                
                // 检查行是否为空
                if (isRowEmpty(currentRow)) {
                    continue;
                }
                
                SeatDTO seat = new SeatDTO();
                
                // 读取座位号
                Cell seatNumberCell = currentRow.getCell(0);
                if (seatNumberCell != null) {
                    if (seatNumberCell.getCellType() == CellType.NUMERIC) {
                        seat.setSeatNumber((int) seatNumberCell.getNumericCellValue());
                    } else if (seatNumberCell.getCellType() == CellType.STRING) {
                        try {
                            seat.setSeatNumber(Integer.parseInt(seatNumberCell.getStringCellValue().trim()));
                        } catch (NumberFormatException e) {
                            // 如果无法解析为数字，则跳过该行
                            continue;
                        }
                    }
                }
                
                // 读取学生姓名
                Cell studentNameCell = currentRow.getCell(1);
                if (studentNameCell != null) {
                    seat.setStudentName(studentNameCell.getStringCellValue().trim());
                }
                
                // 读取是否可用
                Cell availableCell = currentRow.getCell(2);
                boolean isAvailable = true; // 默认可用
                
                if (availableCell != null) {
                    if (availableCell.getCellType() == CellType.BOOLEAN) {
                        isAvailable = availableCell.getBooleanCellValue();
                    } else if (availableCell.getCellType() == CellType.STRING) {
                        String value = availableCell.getStringCellValue().trim().toLowerCase();
                        isAvailable = "true".equals(value) || "是".equals(value) || "可用".equals(value);
                    }
                }
                
                seat.setAvailable(isAvailable);
                
                seats.add(seat);
            }
        }
        
        return seats;
    }
    
    /**
     * 导出座位数据到Excel
     * @param seats 座位列表
     * @param exam 考试信息
     * @return Excel文件的字节数组输入流
     */
    public static ByteArrayInputStream exportSeatsToExcel(List<Seat> seats, Exam exam) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("座位信息");
            
            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建考试信息行
            Row examInfoRow = sheet.createRow(0);
            Cell examInfoCell = examInfoRow.createCell(0);
            examInfoCell.setCellValue("考试信息：" + exam.getSubject() + " - " + exam.getRoom() + " - " + 
                    exam.getExamDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " +
                    exam.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "~" +
                    exam.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            
            // 创建标题行
            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("座位号");
            headerRow.createCell(1).setCellValue("学生姓名");
            headerRow.createCell(2).setCellValue("是否可用");
            
            // 应用标题样式
            for (int i = 0; i < 3; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 2;
            for (Seat seat : seats) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(seat.getSeatNumber());
                
                if (seat.getStudentName() != null && !seat.getStudentName().isEmpty()) {
                    row.createCell(1).setCellValue(seat.getStudentName());
                } else {
                    row.createCell(1).setCellValue("");
                }
                
                row.createCell(2).setCellValue(seat.getAvailable() != null && seat.getAvailable() ? "是" : "否");
            }
            
            // 自动调整列宽
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    
    /**
     * 检查行是否为空
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        
        return true;
    }
}