package com.hekademos.hekademos_backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeBackendService {

    @Value("${youtube.api.key:}")
    private String apiKey;

    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean testApiKey() {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                System.out.println("❌ YouTube API Key no configurada");
                return false;
            }

            String testUrl = BASE_URL + "/videos?part=snippet&chart=mostPopular&maxResults=1&key=" + apiKey;
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ YouTube API Key funciona correctamente");
                return true;
            } else {
                System.out.println("❌ Error con YouTube API Key: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ Error probando YouTube API: " + e.getMessage());
            return false;
        }
    }

    public String searchChannelByHandle(String handle) {
        try {
            if (!testApiKey())
                return null;

            String cleanHandle = handle.startsWith("@") ? handle.substring(1) : handle;
            String searchUrl = BASE_URL + "/search?part=snippet&q=" + cleanHandle +
                    "&type=channel&maxResults=10&key=" + apiKey;

            ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            JsonNode data = objectMapper.readTree(response.getBody());
            JsonNode items = data.get("items");

            if (items != null && items.size() > 0) {
                // Buscar coincidencia con Hekademos
                for (JsonNode item : items) {
                    String title = item.get("snippet").get("title").asText().toLowerCase();
                    if (title.contains("hekademos")) {
                        return item.get("snippet").get("channelId").asText();
                    }
                }
                // Si no encuentra, tomar el primero
                return items.get(0).get("snippet").get("channelId").asText();
            }

            return null;
        } catch (Exception e) {
            System.out.println("Error buscando canal: " + e.getMessage());
            return null;
        }
    }

    public List<YouTubeShortData> getChannelShorts(String channelId) {
        List<YouTubeShortData> shorts = new ArrayList<>();

        try {
            System.out.println("🔍 Buscando shorts para canal: " + channelId);

            // 1. Buscar videos del canal
            String searchUrl = BASE_URL + "/search?part=snippet&channelId=" + channelId +
                    "&type=video&order=date&maxResults=50&key=" + apiKey;

            System.out.println("📡 Consultando YouTube API...");
            ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);

            if (!searchResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("❌ Error en búsqueda: " + searchResponse.getStatusCode());
                System.out.println("❌ NO se guardarán datos mock - falló la búsqueda");
                return new ArrayList<>(); // ✅ Devolver lista vacía en lugar de mock
            }

            JsonNode searchData = objectMapper.readTree(searchResponse.getBody());
            JsonNode searchItems = searchData.get("items");

            if (searchItems == null || searchItems.size() == 0) {
                System.out.println("⚠️ No se encontraron videos en el canal");
                System.out.println("❌ NO se guardarán datos mock - canal sin videos");
                return new ArrayList<>(); // ✅ Devolver lista vacía en lugar de mock
            }

            System.out.println("📹 Encontrados " + searchItems.size() + " videos en total");

            // 2. Obtener IDs de videos
            List<String> videoIds = new ArrayList<>();
            for (JsonNode item : searchItems) {
                videoIds.add(item.get("id").get("videoId").asText());
            }

            // 3. Obtener detalles de duración
            String videoIdsStr = String.join(",", videoIds);
            String detailsUrl = BASE_URL + "/videos?part=snippet,contentDetails&id=" +
                    videoIdsStr + "&key=" + apiKey;

            System.out.println("📊 Obteniendo detalles de " + videoIds.size() + " videos...");
            ResponseEntity<String> detailsResponse = restTemplate.getForEntity(detailsUrl, String.class);

            if (!detailsResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("❌ Error obteniendo detalles: " + detailsResponse.getStatusCode());
                System.out.println("❌ NO se guardarán datos mock - falló obtener detalles");
                return new ArrayList<>(); // ✅ Devolver lista vacía en lugar de mock
            }

            JsonNode detailsData = objectMapper.readTree(detailsResponse.getBody());
            JsonNode detailsItems = detailsData.get("items");

            int shortCount = 0;
            // 4. Filtrar shorts (duración <= 60 segundos)
            for (JsonNode video : detailsItems) {
                String duration = video.get("contentDetails").get("duration").asText();
                int durationSeconds = parseDuration(duration);
                String title = video.get("snippet").get("title").asText();

                System.out.println("🎥 Video: '" + title + "' - Duración: " + durationSeconds + "s");

                if (durationSeconds <= 60) {
                    shortCount++;
                    YouTubeShortData shortData = new YouTubeShortData();
                    shortData.title = title;
                    shortData.videoId = video.get("id").asText();
                    shortData.url = "https://www.youtube.com/shorts/" + shortData.videoId;

                    JsonNode thumbnails = video.get("snippet").get("thumbnails");
                    if (thumbnails.has("maxresdefault")) {
                        shortData.thumbnail = thumbnails.get("maxresdefault").get("url").asText(); // 1280x720
                        System.out.println("🖼️ Usando calidad máxima (1280x720) para: " + title);
                    } else if (thumbnails.has("sddefault")) {
                        shortData.thumbnail = thumbnails.get("sddefault").get("url").asText(); // 640x480
                        System.out.println("🖼️ Usando calidad estándar (640x480) para: " + title);
                    } else if (thumbnails.has("hqdefault")) {
                        shortData.thumbnail = thumbnails.get("hqdefault").get("url").asText(); // 480x360
                        System.out.println("🖼️ Usando calidad alta (480x360) para: " + title);
                    } else if (thumbnails.has("mqdefault")) {
                        shortData.thumbnail = thumbnails.get("mqdefault").get("url").asText(); // 320x180
                        System.out.println("🖼️ Usando calidad media (320x180) para: " + title);
                    } else {
                        shortData.thumbnail = thumbnails.get("default").get("url").asText(); // 120x90
                        System.out.println("⚠️ Usando calidad baja (120x90) para: " + title);
                    }

                    shortData.publishedAt = video.get("snippet").get("publishedAt").asText();
                    shorts.add(shortData);
                    System.out.println("✅ Short agregado: " + title);
                } else {
                    System.out.println("⏭️ Video largo ignorado: " + title);
                }
            }

            System.out.println(
                    "📋 Total shorts REALES encontrados: " + shortCount + " de " + detailsItems.size() + " videos");

        } catch (Exception e) {
            System.out.println("❌ Error obteniendo shorts: " + e.getMessage());
            e.printStackTrace();
            System.out.println("❌ NO se guardarán datos mock - error en proceso");
            return new ArrayList<>(); // ✅ Devolver lista vacía en lugar de mock
        }

        return shorts; // ✅ Devolver solo datos reales (puede ser lista vacía)
    }

    private int parseDuration(String duration) {
        try {
            // Formato ISO 8601: PT1M30S = 1 minuto 30 segundos
            int hours = 0, minutes = 0, seconds = 0;

            if (duration.contains("H")) {
                String[] parts = duration.split("H");
                hours = Integer.parseInt(parts[0].replace("PT", ""));
                duration = parts[1];
            } else {
                duration = duration.replace("PT", "");
            }

            if (duration.contains("M")) {
                String[] parts = duration.split("M");
                minutes = Integer.parseInt(parts[0]);
                duration = parts[1];
            }

            if (duration.contains("S")) {
                seconds = Integer.parseInt(duration.replace("S", ""));
            }

            return hours * 3600 + minutes * 60 + seconds;
        } catch (Exception e) {
            return 0;
        }
    }

    // Clase interna para datos de YouTube Short
    public static class YouTubeShortData {
        public String title;
        public String videoId;
        public String url;
        public String thumbnail;
        public String publishedAt;
    }
}