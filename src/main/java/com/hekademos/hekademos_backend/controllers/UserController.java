package com.hekademos.hekademos_backend.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

import com.hekademos.hekademos_backend.dto.ApiResponse;
import com.hekademos.hekademos_backend.services.IUserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173", "https://hekademos.vercel.app" })
public class UserController {
    @Autowired
    private IUserService userService;

    // 🔧 Método privado para extraer userId
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        return ((Number) details.get("user_id")).longValue();
    }

    // 🔧 Método privado para verificar si es admin
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Usuarios obtenidos con éxito", userService.getAllUsers()));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        // Permitir si es admin o si el email autenticado coincide con el requerido
        if (!isAdmin(authentication) && !authentication.getName().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido con éxito", userService.getUserByEmail(email)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        Long authUserId;
        try {
            authUserId = extractUserIdFromAuthentication(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        if (!isAdmin(authentication) && !authUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido con éxito", userService.getUserById(id)));
    }

    @GetMapping("/routine/{routineLink}")
    public ResponseEntity<?> getUserByRoutineLink(@PathVariable String routineLink, Authentication authentication) {
        // Permitimos acceso público si se desea — pero en este caso lo restringimos a admin o al propietario de la rutina
        // Primero obtenemos el usuario asociado al enlace
        var user = userService.getUserByRoutineLink(routineLink);

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido con éxito", null));
        }

        if (authentication == null) {
            // No autenticado: denegar acceso a datos privados
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        Long authUserId;
        try {
            authUserId = extractUserIdFromAuthentication(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        if (!isAdmin(authentication) && !authUserId.equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido con éxito", user));
    }

    @PutMapping("/{email}/routine/{routineLink}")
    public ResponseEntity<?> updateRoutineLink(@PathVariable String email, @PathVariable String routineLink,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("No autenticado"));
        }

        // Permitir si es admin o si el email autenticado coincide con el email a modificar
        if (!isAdmin(authentication) && !authentication.getName().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Acceso denegado"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Enlace de rutina actualizado con éxito",
                userService.updateRoutineLink(email, routineLink)));
    }
}
