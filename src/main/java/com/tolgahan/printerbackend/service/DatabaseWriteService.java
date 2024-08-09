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
import java.io.InputStream;
import javax.net.ssl.SSLContext;

import java.time.LocalDateTime;
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

    @Scheduled(cron = "* 41 15 * * *") // saniye, dakika, saat, gün, ay, yıl
    //@Scheduled(fixedRate = 100000000) // 10 seconds in milliseconds
    public void fetchDataAndSaveToDatabase() {
        System.out.println("Starting fetchDataAndSaveToDatabase");
        ArrayList<Printer> printers = getAllPrinterIPs();

        for (Printer printer : printers) {
            fetchTotalAndWriteToDatabase(printer);
        }
        System.out.println("Ending fetchDataAndSaveToDatabase");

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
        HashMap<String, Integer> totalPages = null;
        try {
            if (printer.getModel().toLowerCase().contains("PageWide".toLowerCase())) {
                totalPages = fetchFromPageWide(printer);
            } else if (printer.getModel().toLowerCase().contains("Color".toLowerCase())) {
                totalPages = fetchFromLaserColor(printer);
            } else if (printer.getModel().toLowerCase().contains("LaserJet".toLowerCase())) {
                totalPages = fetchFromLaser(printer);
            }

            if (totalPages != null) {
                writeToDatabase(printer.getId(), totalPages);
                logger.info("Total pages of printer id: {} | model: {} | ip: {} written to database: {}", printer.getId(), printer.getModel(), printer.getIp(), totalPages);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("error occurred while fetching total pages: {}", printer.getIp());
        }

    }

    private HashMap<String, Integer> fetchFromPageWide(Printer printer) {
        // This is HP PageWide Managed MFP P77740z AC & HP PageWide MFP P57750 XC printers
        String apiUrl = "http://" + printer.getIp() + "/DevMgmt/ProductUsageDyn.xml";
        try {
            // Fetching data
            String response = restTemplate.getForObject(apiUrl, String.class);
            return parseXMLGetTotal(response);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    public HashMap<String, Integer> fetchFromLaser(Printer printer) throws Exception {
        /// This is for HP LaserJet E60155 & HP LaserJet MFP E52645 printers
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
            Element monoElement = document.getElementById("UsagePage.EquivalentImpressionsTable.Total.Total");

            if (monoElement != null) {
                HashMap<String, Integer> map = new HashMap<>();
                map.put("mono", Integer.parseInt(monoElement.text().replace(",", "").split("\\.")[0]));

                return map;
            }
            return null;


        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    public HashMap<String, Integer> fetchFromLaserColor(Printer printer) throws Exception {
        /// This is for HP Color LaserJet MFP E87640 printers
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
            Element monoElement = document.getElementById("UsagePage.EquivalentImpressionsTable.Monochrome.Total");
            Element colorElement = document.getElementById("UsagePage.EquivalentImpressionsTable.Color.Total");

            if (monoElement != null && colorElement != null) {
                HashMap<String, Integer> map = new HashMap<>();
                map.put("mono", Integer.parseInt(monoElement.text().replace(",", "").split("\\.")[0]));
                map.put("color", Integer.parseInt(colorElement.text().replace(",", "").split("\\.")[0]));

                return map;
            }
            return null;


        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }


    public HashMap<String, Integer> parseXMLGetTotal(String xmlContent) {
        try {
            // Create a DocumentBuilderFactory and DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML content
            InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Extract the content of the "dd:TotalImpressions" element
            NodeList nodeListMono = document.getElementsByTagName("dd:MonochromeImpressions");
            NodeList nodeListColor = document.getElementsByTagName("dd:ColorImpressions");


            if (nodeListMono.getLength() > 0 && nodeListColor.getLength() > 0) {
                HashMap<String, Integer> map = new HashMap<>();
                map.put("color", Integer.parseInt(nodeListColor.item(0).getTextContent()));
                map.put("mono", Integer.parseInt(nodeListMono.item(0).getTextContent()));
                return map;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public void writeToDatabase(Integer printerId, HashMap<String, Integer> totalPages) {
        LocalDateTime datetime = LocalDateTime.now();
        String sql;

        if (totalPages.get("color") == null) {
            sql = "INSERT INTO usages (printerId, date, monoTotal) VALUES (?, ?, ?)";
            logger.info("Total pages of printer id: {} | model: {} | ip: written to database{}", printerId, datetime ,totalPages.get("mono"));

            jdbcTemplate.update(sql, printerId, datetime, totalPages.get("mono"));
        } else {
            sql = "INSERT INTO usages (printerId, date, monoTotal, colorTotal) VALUES (?, ?, ?, ?)";
            logger.info("Total pages of printer id: {} | model: {} | ip: written to database:{}", printerId, totalPages.get("mono"),totalPages.get("color"));

            jdbcTemplate.update(sql, printerId, datetime, totalPages.get("mono"), totalPages.get("color"));
        }
    }


}
