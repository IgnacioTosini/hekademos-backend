package com.hekademos.hekademos_backend.repositories;

import com.hekademos.hekademos_backend.entities.Exercise;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Optional<Exercise> findByName(String name);

    Optional<Exercise> findByVideoUrl(String videoUrl);

    // Nuevos métodos para búsquedas múltiples
    List<Exercise> findByNameContainingIgnoreCase(String name);

    List<Exercise> findByVideoUrlContaining(String videoUrl);

    // Métodos específicos para YouTube
    Page<Exercise> findByIsShortTrue(Pageable pageable);

    Optional<Exercise> findByYoutubeVideoId(String youtubeVideoId);

    @Query("SELECT e FROM Exercise e WHERE e.isShort = true AND e.syncedAt < :cutoffDate")
    List<Exercise> findShortsNeedingSync(
            @Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    @Query("SELECT e.youtubeVideoId FROM Exercise e WHERE e.youtubeVideoId IS NOT NULL")
    Set<String> findAllYoutubeVideoIds();

}