package com.tolgahan.printerbackend.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class ExcelToDB {

    private static final String EXCEL_FILE_PATH = "C:\\Users\\Administrator\\Desktop\\Projects\\printer-backend\\src\\main\\java\\com\\tolgahan\\printerbackend\\utils\\yzc.xlsx";
    private static JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        // Initialize DataSource and JdbcTemplate
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/PrinterUsage");
        //TODO: Write your username & password
        dataSource.setUsername("*****");
        dataSource.setPassword("*****");

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
}
