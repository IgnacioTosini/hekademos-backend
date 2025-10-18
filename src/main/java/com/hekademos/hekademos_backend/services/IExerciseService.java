package com.hekademos.hekademos_backend.services;

import java.util.List;
import java.util.Optional;

import com.hekademos.hekademos_backend.entities.Exercise;

public interface IExerciseService {
    List<Exercise> getAllExercises();

    Optional<Exercise> getExerciseById(Long id);

    Optional<Exercise> getExerciseByName(String name);

    Optional<Exercise> getExerciseByYoutubeUrl(String youtubeUrl);

    List<Exercise> getShorts();

    List<Exercise> getExercisesByName(String name); // Para búsqueda multiple

    List<Exercise> getExercisesByYoutubeUrl(String youtubeUrl); // Para búsqueda multiple

    boolean needsSync(); // Verificar si necesita sincronización

    Exercise createExercise(Exercise exercise);

    Exercise updateExercise(Long id, Exercise exercise);

    boolean deleteExercise(Long id);
}