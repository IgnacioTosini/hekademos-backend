package com.hekademos.hekademos_backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hekademos.hekademos_backend.entities.Exercise;
import com.hekademos.hekademos_backend.repositories.ExerciseRepository;

@Service
public class ExerciseService implements IExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Exercise> getExerciseByName(String name) {
        return exerciseRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Exercise> getExerciseByYoutubeUrl(String youtubeUrl) {
        return exerciseRepository.findByVideoUrl(youtubeUrl);
    }

    // Nuevos métodos para YouTube Shorts
    @Override
    @Transactional(readOnly = true)
    public List<Exercise> getShorts() {
        return exerciseRepository.findByIsShortTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesByName(String name) {
        return exerciseRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesByYoutubeUrl(String youtubeUrl) {
        return exerciseRepository.findByVideoUrlContaining(youtubeUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean needsSync() {
        List<Exercise> shorts = getShorts();
        if (shorts.isEmpty()) {
            return true;
        }

        // Verificar si la última sincronización fue hace más de 24 horas
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return shorts.stream()
                .anyMatch(exercise -> exercise.getSyncedAt() == null ||
                        exercise.getSyncedAt().isBefore(twentyFourHoursAgo));
    }

    @Override
    @Transactional
    public Exercise createExercise(Exercise exercise) {
        // Verificar por nombre solo si no es un short
        if (exercise.getIsShort() == null || !exercise.getIsShort()) {
            Optional<Exercise> existingByName = exerciseRepository.findByName(exercise.getName());
            if (existingByName.isPresent()) {
                throw new IllegalArgumentException("El nombre del ejercicio ya existe");
            }
        }

        // Verificar por URL de video
        Optional<Exercise> existingByUrl = exerciseRepository.findByVideoUrl(exercise.getVideoUrl());
        if (existingByUrl.isPresent()) {
            throw new IllegalArgumentException("La URL de YouTube del ejercicio ya existe");
        }

        // Verificar por YouTube Video ID si está presente
        if (exercise.getYoutubeVideoId() != null) {
            Optional<Exercise> existingByVideoId = exerciseRepository
                    .findByYoutubeVideoId(exercise.getYoutubeVideoId());
            if (existingByVideoId.isPresent()) {
                throw new IllegalArgumentException("El video de YouTube ya existe");
            }
        }

        return exerciseRepository.save(exercise);
    }

    @Override
    @Transactional
    public Exercise updateExercise(Long id, Exercise exercise) {
        return exerciseRepository.findById(id)
                .map(existingExercise -> {
                    // Verificar duplicados solo si se está cambiando
                    if (!existingExercise.getName().equals(exercise.getName())) {
                        Optional<Exercise> existingByName = exerciseRepository.findByName(exercise.getName());
                        if (existingByName.isPresent() && !existingByName.get().getId().equals(id)) {
                            throw new IllegalArgumentException("El nombre del ejercicio ya existe");
                        }
                    }

                    if (!existingExercise.getVideoUrl().equals(exercise.getVideoUrl())) {
                        Optional<Exercise> existingByUrl = exerciseRepository.findByVideoUrl(exercise.getVideoUrl());
                        if (existingByUrl.isPresent() && !existingByUrl.get().getId().equals(id)) {
                            throw new IllegalArgumentException("La URL de YouTube del ejercicio ya existe");
                        }
                    }

                    // Actualizar todos los campos
                    existingExercise.setName(exercise.getName());
                    existingExercise.setVideoUrl(exercise.getVideoUrl());
                    existingExercise.setYoutubeVideoId(exercise.getYoutubeVideoId());
                    existingExercise.setThumbnailUrl(exercise.getThumbnailUrl());
                    existingExercise.setPublishedAt(exercise.getPublishedAt());
                    existingExercise.setIsShort(exercise.getIsShort());
                    existingExercise.setSyncedAt(exercise.getSyncedAt());

                    return exerciseRepository.save(existingExercise);
                })
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado"));
    }

    @Override
    @Transactional
    public boolean deleteExercise(Long id) {
        try {
            if (exerciseRepository.existsById(id)) {
                exerciseRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el ejercicio: " + e.getMessage(), e);
        }
    }
}