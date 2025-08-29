package org.yardimci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RotaYardimcisi {

    // Basit cache yapısı: Koordinat çiftleri için oluşturulan key ile RotaBilgisi nesnesini saklıyoruz.
    private static final Map<String, RotaBilgisi> cache = new HashMap<>();
    private static int apiCallCount = 0; // API çağrı sayısını tutmak için sayaç
    private static final String API_KEY = "ENTER_YOUR_API_KEY"; // Kendi ORS API anahtarınızı buraya ekleyin

    // Rota bilgilerini içeren sınıf
    public static class RotaBilgisi {
        private List<double[]> coordinates;
        private double mesafe; // metre cinsinden
        private double sure;   // saniye cinsinden

        public RotaBilgisi(List<double[]> coordinates, double mesafe, double sure) {
            this.coordinates = coordinates;
            this.mesafe = mesafe;
        }

        public List<double[]> getCoordinates() {
            return coordinates;
        }

        public double getMesafe() {
            return mesafe;
        }

        public double getMesafeKm() {
            return mesafe / 1000.0;
        }

        public double getSureDakika() {
            return sure / 60.0;
        }
    }

    // Metot: Önce cache kontrolü yapar, yoksa API çağrısı gerçekleştirir.
    public static RotaBilgisi getRotaBilgisi(double startLat, double startLon, double endLat, double endLon) {
        String key = String.format(Locale.US, "%.5f,%.5f_%.5f,%.5f", startLat, startLon, endLat, endLon);

        // Cache'de varsa sonucu döndür
        if (cache.containsKey(key)) {
            System.out.println("Cache hit for key: " + key);
            return cache.get(key);
        }

        System.out.println("Cache miss for key: " + key + ". Making API call...");
        apiCallCount++; // API çağrı sayısını artır
        long startTime = System.currentTimeMillis(); // Başlangıç zamanı

        List<double[]> routeCoordinates = new ArrayList<>();
        double mesafe = 0;
        double sure = 0;

        try {
            // ORS API URL: koordinat sırası lon,lat şeklinde
            String urlStr = String.format(Locale.US, "https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%.6f,%.6f&end=%.6f,%.6f", API_KEY, startLon, startLat, endLon, endLat);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("HTTP Hata Kodu: " + responseCode);
                return new RotaBilgisi(routeCoordinates, mesafe, sure);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content.toString());
            JsonNode features = root.path("features");
            if (!features.isArray() || features.size() == 0) {
                System.err.println("Hata: ORS API'den rota verisi alınamadı.");
                return new RotaBilgisi(routeCoordinates, mesafe, sure);
            }

            JsonNode feature = features.get(0);
            JsonNode properties = feature.path("properties");
            JsonNode summary = properties.path("summary");
            mesafe = summary.path("distance").asDouble(); // metre cinsinden
            sure = summary.path("duration").asDouble();   // saniye cinsinden

            JsonNode geometry = feature.path("geometry");
            JsonNode coordinates = geometry.path("coordinates");
            if (!coordinates.isArray() || coordinates.size() == 0) {
                System.err.println("Hata: Geometri verisi boş döndü.");
                return new RotaBilgisi(routeCoordinates, mesafe, sure);
            }

            for (JsonNode coord : coordinates) {
                if (coord.size() < 2) {
                    System.err.println("Hata: Eksik koordinat verisi bulundu.");
                    continue;
                }
                double lon = coord.get(0).asDouble();
                double lat = coord.get(1).asDouble();
                routeCoordinates.add(new double[]{lat, lon});
            }
        } catch (Exception e) {
            System.err.println("Hata: " + e.getMessage());
            e.printStackTrace();
        }

        RotaBilgisi result = new RotaBilgisi(routeCoordinates, mesafe, sure);
        cache.put(key, result); // Sonucu cache'e ekle

        long endTime = System.currentTimeMillis(); // Bitiş zamanı
        long duration = endTime - startTime; // Süreyi hesapla
        System.out.println("API call duration: " + duration + " ms for key: " + key);
        System.out.println("Total API calls made: " + apiCallCount);

        return result;
    }

    // Geriye uyumluluk için eski metodu koruyoruz
    public static List<double[]> getRouteCoordinates(double startLat, double startLon, double endLat, double endLon) {
        return getRotaBilgisi(startLat, startLon, endLat, endLon).getCoordinates();
    }
}
