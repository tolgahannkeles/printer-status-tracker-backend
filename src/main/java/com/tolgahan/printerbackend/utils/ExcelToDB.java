package com.tolgahan.printerbackend.utils;

import com.tolgahan.printerbackend.model.StatusExcel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ExcelToDB {

    private static final String EXCEL_FILE_PATH = "C:\\Users\\Administrator\\Desktop\\Projects\\printer-backend\\src\\main\\java\\com\\tolgahan\\printerbackend\\utils\\yzc.xlsx";
    private static JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        // Initialize DataSource and JdbcTemplate
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/PrinterUsage");
        //TODO: Write your username & password
        dataSource.setUsername("root");
        dataSource.setPassword("200338");

        jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            readExcelAndInsertToDB(EXCEL_FILE_PATH);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void readExcelAndInsertToDB(String filePath) throws IOException, SQLException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            String name = row.getCell(0).getStringCellValue();
            String model = row.getCell(1).getStringCellValue();
            String ip = row.getCell(2).getStringCellValue();
            String serialNo = row.getCell(3).getStringCellValue();

            insertDataIntoDB(serialNo, ip, name, model);
        }

        workbook.close();
        fileInputStream.close();
    }

    private static void insertDataIntoDB(String serialNo, String ip, String name, String model) throws SQLException {
        String sql = "INSERT INTO printer (serialNo, ip, name, model) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, serialNo, ip, name, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToExcel(String path,String workbookStr, List<StatusExcel> statusList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Yeni bir sayfa oluştur
            Sheet sheet = workbook.createSheet(workbookStr);

            // Başlık satırını oluştur
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("IP");
            headerRow.createCell(2).setCellValue("Name");
            headerRow.createCell(3).setCellValue("Date");
            headerRow.createCell(4).setCellValue("Monthly Mono");
            headerRow.createCell(5).setCellValue("Monthly Color");
            headerRow.createCell(6).setCellValue("Total Mono");
            headerRow.createCell(7).setCellValue("Total Color");

            // Veri satırlarını oluştur
            int rowNum = 1;
            for (StatusExcel status : statusList) {
                Row dataRow = sheet.createRow(rowNum++);

                // ID
                dataRow.createCell(0).setCellValue(status.getId());

                // IP
                dataRow.createCell(1).setCellValue(status.getIp() != null ? status.getIp() : "");

                // Name
                dataRow.createCell(2).setCellValue(status.getName() != null ? status.getName() : "");

                // Date
                Cell dateCell = dataRow.createCell(3);
                if (status.getDate() != null) {
                    dateCell.setCellValue(status.getDate().toString());
                } else {
                    dateCell.setCellValue(""); // Boş hücre
                }

                // Monthly Mono
                dataRow.createCell(4).setCellValue(status.getMonthlyMono() != null ? status.getMonthlyMono() : 0);

                // Monthly Color
                dataRow.createCell(5).setCellValue(status.getMonthlyColor() != null ? status.getMonthlyColor() : 0);

                // Total Mono
                dataRow.createCell(6).setCellValue(status.getTotalMono() != null ? status.getTotalMono() : 0);

                // Total Color
                dataRow.createCell(7).setCellValue(status.getTotalColor() != null ? status.getTotalColor() : 0);
            }

            // Excel dosyasını yazma
            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel dosyası başarıyla oluşturuldu: " + path);

        } catch (IOException e) {
            System.err.println("Excel dosyası oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }
}
