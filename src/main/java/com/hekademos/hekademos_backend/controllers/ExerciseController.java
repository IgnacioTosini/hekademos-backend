package com.hekademos.hekademos_backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hekademos.hekademos_backend.dto.ApiResponse;
import com.hekademos.hekademos_backend.entities.Exercise;
import com.hekademos.hekademos_backend.services.IExerciseService;
import com.hekademos.hekademos_backend.services.YouTubeSyncScheduler;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
@RequestMapping("/api/exercises")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
public class ExerciseController {
    @Autowired
    private IExerciseService exerciseService;

    @Autowired
    private YouTubeSyncScheduler youtubeSyncScheduler;

    @GetMapping
    public ResponseEntity<?> getAllExercises() {
        return ResponseEntity.ok(ApiResponse.ok("Ejercicios obtenidos con éxito", exerciseService.getAllExercises()));
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<?> getExerciseById(@PathVariable Long exerciseId) {
        if (exerciseId == null || exerciseId <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("ID de ejercicio no válido"));
        }

        Optional<Exercise> exerciseOpt = exerciseService.getExerciseById(exerciseId);
        if (exerciseOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Ejercicio no encontrado"));
        }

        return exerciseOpt
                .map(exercise -> ResponseEntity.ok(ApiResponse.ok("Ejercicio obtenido con éxito", exercise)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<?> getExercisesByName(@PathVariable String name) {
        List<Exercise> exercises = exerciseService.getExercisesByName(name);
        return ResponseEntity.ok(ApiResponse.ok("Ejercicios obtenidos con éxito", exercises));
    }

    @GetMapping("/by-youtube-url")
    public ResponseEntity<?> getExercisesByYoutubeUrl(@RequestParam String youtubeUrl) {
        List<Exercise> exercises = exerciseService.getExercisesByYoutubeUrl(youtubeUrl);
        return ResponseEntity.ok(ApiResponse.ok("Ejercicios obtenidos con éxito", exercises));
    }

    // Nuevos endpoints para YouTube Shorts
    @GetMapping("/shorts")
    public ResponseEntity<?> getShorts() {
        List<Exercise> shorts = exerciseService.getShorts();
        return ResponseEntity.ok(ApiResponse.ok("Shorts obtenidos con éxito", shorts));
    }

    @GetMapping("/sync-status")
    public ResponseEntity<?> needsSync() {
        boolean needsSync = exerciseService.needsSync();
        return ResponseEntity.ok(ApiResponse.ok("Estado de sincronización verificado", needsSync));
    }

    @PostMapping
    public ResponseEntity<?> createExercise(@Valid @RequestBody Exercise exercise, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }

        try {
            Exercise createdExercise = exerciseService.createExercise(exercise);
            return ResponseEntity.status(201).body(ApiResponse.ok("Ejercicio creado con éxito", createdExercise));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error al crear el ejercicio"));
        }
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<?> updateExercise(@PathVariable Long exerciseId, @Valid @RequestBody Exercise exercise,
            BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }

        try {
            Exercise updatedExercise = exerciseService.updateExercise(exerciseId, exercise);
            return ResponseEntity.ok(ApiResponse.ok("Ejercicio actualizado con éxito", updatedExercise));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error al actualizar el ejercicio"));
        }
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long exerciseId) {
        if (exerciseId == null || exerciseId <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("ID de ejercicio no válido"));
        }

        try {
            boolean deleted = exerciseService.deleteExercise(exerciseId);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.ok("Ejercicio eliminado con éxito", null));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Ejercicio no encontrado"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error al eliminar el ejercicio: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error interno del servidor"));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<?> manualSync() {
        try {
            String result = youtubeSyncScheduler.manualSync();
            return ResponseEntity.ok(ApiResponse.ok("Sincronización iniciada", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Error en sincronización: " + e.getMessage()));
        }
    }

    // 🔧 Validación de campos
    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), "El campo " + error.getField() + " " + error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}