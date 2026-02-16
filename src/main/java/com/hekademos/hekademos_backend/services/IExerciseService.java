package com.hekademos.hekademos_backend.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hekademos.hekademos_backend.entities.Exercise;

public interface IExerciseService {
    List<Exercise> getAllExercises();

    Optional<Exercise> getExerciseById(Long id);

    Optional<Exercise> getExerciseByName(String name);

    Optional<Exercise> getExerciseByYoutubeUrl(String youtubeUrl);

    Page<Exercise> getShorts(Pageable pageable); // Para paginación de shorts

    List<Exercise> getExercisesByName(String name); // Para búsqueda multiple

    List<Exercise> getExercisesByYoutubeUrl(String youtubeUrl); // Para búsqueda multiple

    boolean needsSync(); // Verificar si necesita sincronización

    Exercise createExercise(Exercise exercise);

    Exercise updateExercise(Long id, Exercise exercise);

    boolean deleteExercise(Long id);

    Set<String> getAllYoutubeVideoIds();

    void saveAll(List<Exercise> exercises);

}