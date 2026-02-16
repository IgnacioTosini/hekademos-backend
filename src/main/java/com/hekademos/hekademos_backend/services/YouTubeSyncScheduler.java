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

    private boolean isSyncRunning = false;

    // 🔥 Ejecuta cada 6 horas
    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledSync() {
        performSync("Sincronización automática (6 horas)");
    }

    private synchronized void performSync(String syncType) {

        if (isSyncRunning) {
            System.out.println("⏳ Ya hay una sincronización en curso. Saltando...");
            return;
        }

        isSyncRunning = true;

        try {
            System.out.println("🔄 Iniciando " + syncType + "...");

            if (!exerciseService.needsSync()) {
                System.out.println("⏭️ No es necesario sincronizar todavía");
                return;
            }

            String channelId = youTubeService.searchChannelByHandle(channelHandle);

            if (channelId == null) {
                System.out.println("❌ No se pudo encontrar el canal: " + channelHandle);
                return;
            }

            // 🔥 SOLO traer los videoIds existentes (optimizado)
            Set<String> existingVideoIds = exerciseService.getAllYoutubeVideoIds();

            List<YouTubeShortData> youtubeShorts =
                    youTubeService.getChannelShorts(channelId);

            if (youtubeShorts.isEmpty()) {
                System.out.println("❌ No se encontraron shorts en el canal");
                return;
            }

            // 🔥 Filtrar nuevos
            List<Exercise> newExercises = youtubeShorts.stream()
                    .filter(shortData -> !existingVideoIds.contains(shortData.videoId))
                    .map(this::createExerciseFromShort)
                    .collect(Collectors.toList());

            if (newExercises.isEmpty()) {
                System.out.println("📋 Base de datos ya está actualizada");
                return;
            }

            // 🔥 Batch real
            exerciseService.saveAll(newExercises);

            System.out.println("🎉 " + newExercises.size() + " shorts nuevos agregados");

        } catch (Exception e) {
            System.err.println("❌ Error en sincronización: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isSyncRunning = false;
        }
    }

    private Exercise createExerciseFromShort(YouTubeShortData shortData) {

        LocalDateTime now = LocalDateTime.now();
        Exercise exercise = new Exercise();

        exercise.setName(shortData.title);
        exercise.setVideoUrl(shortData.url);
        exercise.setYoutubeUrl(shortData.url);
        exercise.setYoutubeVideoId(shortData.videoId);
        exercise.setThumbnailUrl(shortData.thumbnail);
        exercise.setIsShort(true);
        exercise.setSyncedAt(now);

        try {
            String dateStr = shortData.publishedAt.replace("Z", "");
            exercise.setPublishedAt(
                    LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        } catch (Exception e) {
            exercise.setPublishedAt(now);
        }

        return exercise;
    }

    // 🔥 Para endpoint manual
    public String manualSync() {
        performSync("Sincronización manual");
        return "Sincronización ejecutada";
    }
}
