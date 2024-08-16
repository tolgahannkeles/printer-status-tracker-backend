package com.tolgahan.printerbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tolgahan.printerbackend.utils.PingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    public PrinterController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {  // Inject ObjectMapper
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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

    @GetMapping("/ping")
    public ResponseEntity<String> pingAllPrinters(@RequestParam(value = "isOk", required = false) Boolean isOk) throws JsonProcessingException {
        logger.info("Start to ping all printers");
        try {
            String sql = "SELECT * FROM printer";
            List<Map<String, Object>> printers = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> printer : printers) {
                String ip = (String) printer.get("ip");
                if (ip != null && !ip.isEmpty()) {
                    boolean pingResult = PingUtils.isReachable(ip);
                    printer.put("isAvailable", pingResult);
                } else {
                    printer.put("isAvailable", false); // IP adresi yoksa sonuç da false
                }
            }
            System.out.println(isOk);
            List<Map<String, Object>> filteredPrinters;
            if (isOk == null) {
                filteredPrinters = printers;
            } else {
                filteredPrinters = printers.stream()
                        .filter(printer -> (boolean) printer.get("isAvailable") == isOk)
                        .toList();
            }


            String json = objectMapper.writeValueAsString(filteredPrinters);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
    @GetMapping("/{printerId}")
    public ResponseEntity<String> getPrinterById(@PathVariable String printerId) throws JsonProcessingException {
        logger.info("Fetching printer with ID: {}", printerId);
        try {
            Integer ip = Integer.parseInt(printerId);
            String sql = "SELECT * FROM printer WHERE id = ? LIMIT 1";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, ip);
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


    @PostMapping("/byIp")
    public ResponseEntity<?> getPrinterByIp(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        logger.info("Fetch attempt by ip: {}", request.get("ip"));
        String ip = request.get("ip").toString();

        try {
            String sql = "SELECT * FROM printer WHERE ip = ? LIMIT 1";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, ip);
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
        } catch (Exception e) {
            logger.error("Error during fetching printer by ip: {}", ip, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @PostMapping
    public ResponseEntity<?> insertPrinter(@RequestBody Map<String, String> request) throws JsonProcessingException {
        logger.info("Insert printer attempt ip: {}", request.get("ip"));

        try {
            String serialNo = request.get("serialNo");
            String ip = request.get("ip");
            String name = request.get("name");
            String model = request.get("model");
            HashMap<String, String> map = new HashMap<>();

            if (serialNo != null && !serialNo.isEmpty() &&
                    ip != null && !ip.isEmpty() &&
                    name != null && !name.isEmpty() &&
                    model != null && !model.isEmpty()) {
                String sql = "insert into printer(serialNo,ip,name,model) VALUES (?,?,?,?)";
                jdbcTemplate.update(sql, serialNo, ip, name, model);
                map.put("message", "Successfully inserted a new printer");
                String json = objectMapper.writeValueAsString(map);

                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .body(json);
            }

            throw new Exception("serialNo, ip, model and name cannot be null");

        } catch (Exception e) {
            logger.error("Error during inserting new printer", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @DeleteMapping("/byIp")
    public ResponseEntity<String> deletePrinterByIp(@RequestBody Map<String, Object> request) throws JsonProcessingException {
        logger.info("Delete attempt ID: {}", request.get("ip"));
        String ip = request.get("ip").toString();

        try {
            String sql = "delete from printer where ip = ?";
            jdbcTemplate.update(sql, ip);
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Successfully deleted a printer with Ip: " + ip);
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

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
            sqlBuilder.append(" WHERE id = :id");
            params.put("id", printerId);

            String sql = sqlBuilder.toString();
            int response = namedParameterJdbcTemplate.update(sql, params);

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

    @PatchMapping("/byIp")
    public ResponseEntity<String> updatePrinter(
            @RequestBody Map<String, Object> request) throws JsonProcessingException {

        logger.info("Patch attempt: {}", request.get("ip"));

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
            sqlBuilder.append(" WHERE ip = :ip");
            params.put("ip", ip);

            String sql = sqlBuilder.toString();
            int response = namedParameterJdbcTemplate.update(sql, params);

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