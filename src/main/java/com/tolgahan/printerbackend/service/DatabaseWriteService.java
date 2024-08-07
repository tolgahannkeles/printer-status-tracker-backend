package com.tolgahan.printerbackend.service;

import com.tolgahan.printerbackend.model.Printer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.SSLContext;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

@Service
public class DatabaseWriteService {
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(DatabaseWriteService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public DatabaseWriteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 0 6 * * *") // Runs every day at 6 AM
    public void fetchDataAndSaveToDatabase() {
        ArrayList<Printer> printers = getAllPrinterIPs();

        for (Printer printer : printers) {
            fetchTotalAndWriteToDatabase(printer);
        }
        System.out.println("end");
    }


    private ArrayList<Printer> getAllPrinterIPs() {
        ArrayList<Printer> printers = new ArrayList<>();
        String sql = "SELECT id,ip,model FROM printer";

        try {
            List<Printer> temp = jdbcTemplate.query(sql,
                    (rs, rowNum) -> new Printer(
                            Integer.parseInt(rs.getString("id")),
                            rs.getString("ip"),
                            rs.getString("model")
                    )
            );

            printers.addAll(temp);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return printers;
    }

    public void fetchTotalAndWriteToDatabase(Printer printer) {
        Integer totalPages = null;
        try {
            if (printer.getModel().toLowerCase().contains("PageWide".toLowerCase())) {
                totalPages = fetchFromPageWide(printer);
            } else if (printer.getModel().toLowerCase().contains("LaserJet".toLowerCase())) {
                totalPages = fetchFromLaser(printer);
            }

            if (totalPages != null) {
                writeToDatabase(printer.getId(), totalPages);
                logger.info("Total pages of printer {} | {} written to database: {}", printer.getModel(), printer.getIp(), totalPages);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("error occurred while fetching total pages: {}", printer.getIp());
        }

    }

    private Integer fetchFromPageWide(Printer printer) {
        // This is HP PageWide Managed MFP P77740z AC & HP PageWide MFP P57750 XC printers
        String apiUrl = "http://" + printer.getIp() + "/DevMgmt/ProductUsageDyn.xml";
        try {
            // Fetching data
            String response = restTemplate.getForObject(apiUrl, String.class);
            String totalPages = parseXMLGetTotal(response);
            if (totalPages != null) return Integer.parseInt(totalPages);
            return null;

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    public Integer fetchFromLaser(Printer printer) throws Exception {
        /// This is for HP LaserJet E60155 & HP LaserJet MFP E52645 & HP Color LaserJet MFP E87640 printers
        try {
            String url = "https://" + printer.getIp() + "/hp/device/InternalPages/Index?id=UsagePage";
            // Create an SSL context that does not validate certificates
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true) // Trust all certificates
                    .build();
            SSLConnectionSocketFactory sslref = new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslref)
                    .build();

            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String htmlContent = EntityUtils.toString(entity);

            // Parse the HTML using Jsoup
            org.jsoup.nodes.Document document = Jsoup.parse(htmlContent);

            // Extract the "2,260" value
            Element totalElement = document.getElementById("UsagePage.EquivalentImpressionsTable.Total.Total");

            if (totalElement != null) {
                return Integer.parseInt(totalElement.text().replace(",", "").split("\\.")[0]);
            }
            return null;


        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    public String parseXMLGetTotal(String xmlContent) {
        try {
            // Create a DocumentBuilderFactory and DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML content
            InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Extract the content of the "dd:TotalImpressions" element
            NodeList nodeList = document.getElementsByTagName("dd:TotalImpressions");
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public void writeToDatabase(Integer printerId, Integer totalPages) {
        LocalDateTime datetime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date = datetime.format(formatter);

        String sql = "INSERT INTO usages (printerId, date, totalPages) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, printerId, date, totalPages);
    }


}
