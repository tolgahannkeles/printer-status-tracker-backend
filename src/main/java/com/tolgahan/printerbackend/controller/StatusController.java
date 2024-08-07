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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/status")
public class StatusController {
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(StatusController.class);
    private final ObjectMapper objectMapper;  // Add ObjectMapper

    @Autowired
    public StatusController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {  // Inject ObjectMapper
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    @GetMapping("")
    public ResponseEntity<String> getUsages() {
        logger.info("Fetching all usages from the database");
        try {
            String sql = "SELECT * FROM USAGES";

            List<Map<String, Object>> books = jdbcTemplate.queryForList(sql);

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
    public ResponseEntity<String> getUsageById(@PathVariable Long printerId) {
        logger.info("Fetching usage with ID: {}", printerId);
        try {
            String sql = "SELECT * FROM usages WHERE printerId = ?"; // Use a placeholder for bookId

            List<Map<String, Object>> books = jdbcTemplate.queryForList(sql, printerId); // Pass bookId as parameter
            String json = objectMapper.writeValueAsString(books.get(0));
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (DataAccessException | JsonProcessingException e) {
            logger.error("Error fetching or converting book to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the request.");
        }
    }




}