package com.tolgahan.printerbackend.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingUtils {
    public static boolean isReachable(String ipAddress) {
        String os = System.getProperty("os.name").toLowerCase();
        String pingCmd;

        // OS'e bağlı olarak ping komutunu belirle
        if (os.contains("win")) {
            pingCmd = "ping -n 1 " + ipAddress; // Windows
        } else {
            pingCmd = "ping -c 1 " + ipAddress; // Linux/Mac
        }

        try {
            // Ping komutunu çalıştır
            Process process = Runtime.getRuntime().exec(pingCmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean reachable = false;

            // Çıktıyı oku
            while ((line = reader.readLine()) != null) {
                // OS'e bağlı olarak çıkış koduna göre başarımı kontrol et
                if ( line.contains("TTL")) {
                    reachable = true;
                }  else if (line.contains("Destination host unreachable")) {
                    reachable = false;
                }
            }

            // Komutun tamamlanmasını bekle ve çıkış kodunu kontrol et
            int exitCode = process.waitFor();
            return reachable && exitCode == 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
