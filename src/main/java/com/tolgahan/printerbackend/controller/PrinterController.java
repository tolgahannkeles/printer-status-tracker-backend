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

import java.util.HashMap;
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

    @GetMapping
    public ResponseEntity<String> getAllPrinters() throws JsonProcessingException {
        logger.info("Fetching all usages from the database");
        try {
            String sql = "SELECT * FROM printer";

            List<Map<String, Object>> printers = jdbcTemplate.queryForList(sql);
            String json = objectMapper.writeValueAsString(printers);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);
        } catch (Exception e) {
            logger.error("Error fetching or converting authors to JSON", e);
            HashMap<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(error));
        }
    }

    @PostMapping
    public ResponseEntity<?> insertPrinter(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        logger.info("Insert attempt ip: {}", request.get("ip"));

        try {
            String serialNo = request.get("serialNo").toString();
            String ip = request.get("ip").toString();
            String name = request.get("name").toString();
            String model = request.get("model").toString();

            String sql = "insert into printer(serialNo,ip,name,model) VALUES (?,?,?,?)";
            jdbcTemplate.update(sql, serialNo, ip, name, model);
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Successfully inserted a new printer");
            String json = objectMapper.writeValueAsString(map);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);
        } catch (Exception e) {
            logger.error("Error during inserting new printer", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deletePrinterById(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        logger.info("Delete attempt ID: {}", request.get("ip"));
        String ip = request.get("ip").toString();

        try {
            String sql = "delete from printer where ip = ?";
            jdbcTemplate.update(sql, ip);
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Successfully deleted a printer with ID: " + ip);
            String json = objectMapper.writeValueAsString(map);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (Exception e) {
            logger.error("Error during deleting printer ip: {}", ip, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @GetMapping("/{printerId}")
    public ResponseEntity<String> getPrinterById(@PathVariable Integer printerId) throws JsonProcessingException {
        logger.info("Fetching printer with ID: {}", printerId);
        try {
            String sql = "SELECT * FROM printer WHERE id = ? LIMIT 1";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, printerId);
            if (results.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(objectMapper.writeValueAsString(errorResponse));
            }

            String json = objectMapper.writeValueAsString(results.get(0));

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (DataAccessException | JsonProcessingException e) {
            logger.error("Error fetching printer", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }


    @DeleteMapping("/{printerId}")
    public ResponseEntity<String> deletePrinterById(@PathVariable Integer printerId) throws JsonProcessingException {
        logger.info("Delete attempt: {}", printerId);
        try {

            String sql = "delete from printer where id = ?";
            jdbcTemplate.update(sql, printerId);
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Successfully deleted a printer with ID: " + printerId);
            String json = objectMapper.writeValueAsString(map);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (Exception e) {
            logger.error("Error during deleting printer id: {}", printerId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }


    @PatchMapping("/{printerId}")
    public ResponseEntity<String> updatePrinter(
            @PathVariable Integer printerId,
            @RequestBody Map<String, Object> request) throws JsonProcessingException {

        logger.info("Patch attempt: {}", printerId);

        try {
            String serialNo = (String) request.get("serialNo");
            String ip = (String) request.get("ip");
            String name = (String) request.get("name");
            String model = (String) request.get("model");

            // Güncellenmiş değerlerin SQL sorgusuna eklenmesi
            StringBuilder sqlBuilder = new StringBuilder("UPDATE printer SET ");
            Map<String, Object> params = new HashMap<>();

            if (serialNo != null) {
                sqlBuilder.append("serialNo = :serialNo, ");
                params.put("serialNo", serialNo);
            }
            if (ip != null) {
                sqlBuilder.append("ip = :ip, ");
                params.put("ip", ip);
            }
            if (name != null) {
                sqlBuilder.append("name = :name, ");
                params.put("name", name);
            }
            if (model != null) {
                sqlBuilder.append("model = :model, ");
                params.put("model", model);
            }

            // SQL sorgusundan son virgülü kaldırma
            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append(" WHERE id = :printerId");
            params.put("printerId", printerId);

            String sql = sqlBuilder.toString();
            int response = jdbcTemplate.update(sql, params);

            Map<String, String> map = new HashMap<>();
            map.put("message", "Updated rows: " + response);
            String json = objectMapper.writeValueAsString(map);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (Exception e) {
            logger.error("Error during update request", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }


}