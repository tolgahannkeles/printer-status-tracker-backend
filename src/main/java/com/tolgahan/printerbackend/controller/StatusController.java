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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
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

    @GetMapping
    public ResponseEntity<String> getUsages() {
        logger.info("Fetching all usages from the database");
        try {
            String sql = "SELECT * FROM USAGES, PRINTER WHERE PRINTER.ID = USAGES.PRINTERID";

            List<Map<String, Object>> usages = jdbcTemplate.queryForList(sql);

            String json = objectMapper.writeValueAsString(usages);

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

            List<Map<String, Object>> usages = jdbcTemplate.queryForList(sql, printerId); // Pass bookId as parameter
            String json = objectMapper.writeValueAsString(usages);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (DataAccessException | JsonProcessingException e) {
            logger.error("Error fetching or converting book to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the request.");
        }
    }

    @PostMapping("/byDate")
    public ResponseEntity<?> getUsageByDate(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        // Date should be in "yyyy-MM-dd" format
        logger.info("Fetching usage data for date: {}", request.get("date"));

        try {
            // Input date in "yyyy-MM" format
            String date = request.get("date").toString();

            // Query for the user by username
            String datePattern = date + "%";

            // SQL query to filter records between start and end of the day
            String sql = "SELECT * FROM usages,printer WHERE date LIKE ? and printerId=id";
            List<Map<String, Object>> usages = jdbcTemplate.queryForList(sql, datePattern);

            if (usages.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "No usage found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(objectMapper.writeValueAsString(error));
            }

            String json = objectMapper.writeValueAsString(usages);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (Exception e) {
            logger.error("Error during status request by date", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while processing the request.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.writeValueAsString(errorResponse));
        }
    }


    @PostMapping("/byIp")
    public ResponseEntity<?> getUsageByIp(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        logger.info("Status attempt by ip: {}", request.get("ip"));

        try {
            String date = request.get("ip").toString();

            // Query for the user by username
            String sql = "select * from printer join usages on printerId=id WHERE ip = ?";
            List<Map<String, Object>> usages = jdbcTemplate.queryForList(sql, date);

            if (usages.isEmpty()) {
                HashMap<String, String> error = new HashMap<>();
                error.put("message", "No usage found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            String json = objectMapper.writeValueAsString(usages);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);


        } catch (Exception e) {
            logger.error("Error during status request by ip", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while processing the request.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @PostMapping("/totalStatus")
    public ResponseEntity<?> getTotalStatus(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        logger.info("Total status attempt by ip: {}", request.get("ip"));

        try {
            String date = request.get("date").toString();

            // Query for the user by username
            String datePattern = date + "%";

            String sql = "SELECT SUM(monoCount) AS totalMonoCount, SUM(colorCount) AS totalColorCount FROM usages WHERE date LIKE ?";
            List<Map<String, Object>> usages = jdbcTemplate.queryForList(sql, datePattern);

            if (usages.isEmpty()) {
                HashMap<String, String> error = new HashMap<>();
                error.put("message", "No usage found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            String json = objectMapper.writeValueAsString(usages.get(0));
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (Exception e) {
            logger.error("Error during total status request by ip", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while processing the request.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.writeValueAsString(errorResponse));
        }
    }
}