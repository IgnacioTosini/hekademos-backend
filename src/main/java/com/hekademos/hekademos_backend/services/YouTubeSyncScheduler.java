package com.hekademos.hekademos_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hekademos.hekademos_backend.entities.Exercise;
import com.hekademos.hekademos_backend.services.YouTubeBackendService.YouTubeShortData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class YouTubeSyncScheduler {

    @Autowired
    private IExerciseService exerciseService;

    @Autowired
    private YouTubeBackendService youTubeService;

    @Value("${youtube.channel.handle:@hekademos}")
    private String channelHandle;

    // Ejecutar todos los días a las 3:00 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void syncYouTubeShorts() {
        performSync("Sincronización automática diaria");
    }

    // También ejecutar cada 6 horas para testing (opcional)
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 horas
    public void syncYouTubeShortsFrequent() {
        if (exerciseService.needsSync()) {
            performSync("Sincronización frecuente (6 horas)");
        }
    }

    private void performSync(String syncType) {
        try {
            System.out.println("🔄 Iniciando " + syncType + "...");

            if (!exerciseService.needsSync()) {
                System.out.println("⏭️ No es necesario sincronizar todavía");
                return;
            }

            // 1. Buscar canal de YouTube
            System.out.println("🔍 Buscando canal: " + channelHandle);
            String channelId = youTubeService.searchChannelByHandle(channelHandle);

            if (channelId == null) {
                System.out.println("❌ NO se pudo encontrar el canal '" + channelHandle + "'");
                System.out.println("❌ Sincronización CANCELADA - no hay canal válido");
                return; // ✅ No continuar sin canal válido - NO usar mock
            }

            System.out.println("✅ Canal encontrado: " + channelId);

            // 2. Obtener shorts de YouTube (SOLO datos reales)
            System.out.println("📡 Obteniendo shorts REALES de YouTube...");
            List<YouTubeShortData> youtubeShorts = youTubeService.getChannelShorts(channelId);

            if (youtubeShorts.isEmpty()) {
                System.out.println("❌ NO se encontraron shorts en el canal");
                System.out.println("❌ Sincronización CANCELADA - sin shorts reales disponibles");
                System.out.println("❌ NO se usarán datos mock por decisión del usuario");
                return; // ✅ No continuar sin datos reales - NO usar mock
            }

            System.out.println("📺 Encontrados " + youtubeShorts.size() + " shorts REALES en YouTube");

            // 3. Obtener ejercicios existentes
            List<Exercise> existingExercises = exerciseService.getAllExercises();
            Set<String> existingVideoIds = existingExercises.stream()
                    .map(Exercise::getYoutubeVideoId)
                    .filter(videoId -> videoId != null && !videoId.isEmpty())
                    .collect(Collectors.toSet());

            System.out.println("📚 Ejercicios existentes en BD: " + existingExercises.size());

            // 4. Filtrar shorts nuevos
            List<YouTubeShortData> newShorts = youtubeShorts.stream()
                    .filter(shortData -> {
                        boolean isNew = !existingVideoIds.contains(shortData.videoId);
                        if (!isNew) {
                            System.out.println("⏭️ Ya existe: " + shortData.title + " (" + shortData.videoId + ")");
                        }
                        return isNew;
                    })
                    .collect(Collectors.toList());

            System.out.println("✨ " + newShorts.size() + " shorts NUEVOS para agregar");

            if (newShorts.isEmpty()) {
                System.out.println("📋 Todos los shorts reales ya están en la BD");
                return;
            }

            // 5. GUARDAR TODOS LOS SHORTS NUEVOS REALES
            int savedCount = 0;
            int errorCount = 0;
            LocalDateTime now = LocalDateTime.now();

            for (YouTubeShortData shortData : newShorts) {
                try {
                    System.out.println("💾 Guardando SHORT REAL: " + shortData.title);

                    Exercise exercise = new Exercise();
                    exercise.setName(shortData.title);
                    exercise.setVideoUrl(shortData.url);
                    exercise.setYoutubeUrl(shortData.url); // Para compatibilidad con BD
                    exercise.setYoutubeVideoId(shortData.videoId);
                    exercise.setThumbnailUrl(shortData.thumbnail);

                    // Convertir publishedAt
                    try {
                        String dateStr = shortData.publishedAt;
                        if (dateStr.endsWith("Z")) {
                            dateStr = dateStr.replace("Z", "");
                        }
                        exercise.setPublishedAt(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    } catch (Exception dateError) {
                        exercise.setPublishedAt(now);
                    }

                    exercise.setIsShort(true);
                    exercise.setSyncedAt(now);

                    Exercise savedExercise = exerciseService.createExercise(exercise);
                    if (savedExercise != null && savedExercise.getId() != null) {
                        savedCount++;
                        System.out.println("✅ SHORT REAL GUARDADO (#" + savedCount + "): " + shortData.title + " [ID: "
                                + savedExercise.getId() + "]");
                    } else {
                        errorCount++;
                        System.err.println("❌ ERROR: createExercise retornó null para '" + shortData.title + "'");
                    }

                } catch (Exception e) {
                    errorCount++;
                    System.err.println("❌ ERROR guardando '" + shortData.title + "': " + e.getMessage());
                }
            }

            String finalMessage = syncType + " COMPLETADA: " + savedCount + " shorts REALES guardados";
            if (errorCount > 0) {
                finalMessage += " (" + errorCount + " errores)";
            }

            System.out.println("🎉 " + finalMessage);

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO en " + syncType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para sincronización manual desde el controlador
    public String manualSync() {
        try {
            performSync("Sincronización manual");
            return "Sincronización manual exitosa";
        } catch (Exception e) {
            return "Error en sincronización manual: " + e.getMessage();
        }
    }
}