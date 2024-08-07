package com.tolgahan.printerbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/printers")
public class PrinterController {
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(PrinterController.class);
    private final ObjectMapper objectMapper;  // Add ObjectMapper

    @Autowired
    public PrinterController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {  // Inject ObjectMapper
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("")
    public ResponseEntity<String> getPrinters() {
        logger.info("Fetching all usages from the database");
        try {
            String sql = "SELECT * FROM printer";

            List<Map<String, Object>> books = jdbcTemplate.queryForList(sql);

            // Convert to JSON using ObjectMapper
            String json = objectMapper.writeValueAsString(books);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json") // Set content type
                    .body(json);
        } catch (DataAccessException | JsonProcessingException e) {
            logger.error("Error fetching or converting authors to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/{printerId}")
    public ResponseEntity<String> getPrinterById(@PathVariable Long printerId) {
        logger.info("Fetching author with ID: {}", printerId);
        try {
            String sql = "SELECT * FROM printer WHERE id = ?"; // Use a placeholder for bookId

            List<Map<String, Object>> books = jdbcTemplate.queryForList(sql, printerId); // Pass bookId as parameter

            if (books.isEmpty()) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            } else if (books.size() > 1) {
                logger.error("Multiple author found with the same ID: {}", printerId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Multiple books found with the same ID");
            } else {
                String json = objectMapper.writeValueAsString(books.get(0));
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .body(json);
            }
        } catch (DataAccessException | JsonProcessingException e) {
            logger.error("Error fetching or converting book to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the request.");
        }
    }


}