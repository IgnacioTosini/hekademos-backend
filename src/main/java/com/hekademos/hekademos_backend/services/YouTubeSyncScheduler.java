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

            // ✅ CAMBIO 1: PRIMERO mostrar lo que ya tienes en BD
            List<Exercise> existingExercises = exerciseService.getAllExercises();
            System.out.println("📚 Mostrando " + existingExercises.size() + " ejercicios existentes en BD");

            // Si hay ejercicios en BD, el usuario ya puede verlos mientras sincronizamos
            if (!existingExercises.isEmpty()) {
                System.out.println("✅ Usuarios pueden ver contenido existente mientras sincronizamos");
            }

            if (!exerciseService.needsSync()) {
                System.out.println("⏭️ No es necesario sincronizar todavía");
                return;
            }

            // ✅ CAMBIO 2: Sincronización en segundo plano
            System.out.println("� Iniciando sincronización en segundo plano...");
            String channelId = youTubeService.searchChannelByHandle(channelHandle);

            if (channelId == null) {
                System.out.println("❌ NO se pudo encontrar el canal '" + channelHandle + "'");
                return;
            }

            // ✅ CAMBIO 3: Obtener solo IDs de videos existentes para comparación rápida
            Set<String> existingVideoIds = existingExercises.stream()
                    .map(Exercise::getYoutubeVideoId)
                    .filter(videoId -> videoId != null && !videoId.isEmpty())
                    .collect(Collectors.toSet());

            System.out.println("� Obteniendo shorts de YouTube para comparar...");
            List<YouTubeShortData> youtubeShorts = youTubeService.getChannelShorts(channelId);

            if (youtubeShorts.isEmpty()) {
                System.out.println("❌ NO se encontraron shorts en el canal");
                return;
            }

            // ✅ CAMBIO 4: Filtrar y guardar solo nuevos (más eficiente)
            List<YouTubeShortData> newShorts = youtubeShorts.stream()
                    .filter(shortData -> !existingVideoIds.contains(shortData.videoId))
                    .collect(Collectors.toList());

            System.out.println("✨ " + newShorts.size() + " shorts nuevos encontrados para agregar");

            if (newShorts.isEmpty()) {
                System.out.println("📋 Base de datos ya está actualizada");
                return;
            }

            // ✅ CAMBIO 5: Guardar nuevos en lotes para mejor performance
            int savedCount = saveShortsInBatch(newShorts);

            System.out.println("🎉 " + syncType + " COMPLETADA: " + savedCount + " shorts nuevos agregados");

        } catch (Exception e) {
            System.err.println("❌ ERROR en " + syncType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ MÉTODO NUEVO: Guardar en lotes para mejor performance
    private int saveShortsInBatch(List<YouTubeShortData> newShorts) {
        int savedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (YouTubeShortData shortData : newShorts) {
            try {
                Exercise exercise = createExerciseFromShort(shortData, now);
                Exercise savedExercise = exerciseService.createExercise(exercise);

                if (savedExercise != null && savedExercise.getId() != null) {
                    savedCount++;
                    System.out.println("✅ SHORT GUARDADO: " + shortData.title);
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR guardando '" + shortData.title + "': " + e.getMessage());
            }
        }

        return savedCount;
    }

    // ✅ MÉTODO NUEVO: Crear Exercise desde YouTubeShortData
    private Exercise createExerciseFromShort(YouTubeShortData shortData, LocalDateTime now) {
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
            exercise.setPublishedAt(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            exercise.setPublishedAt(now);
        }

        return exercise;
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