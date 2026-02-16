package com.hekademos.hekademos_backend.controllers;

import com.hekademos.hekademos_backend.dto.ApiResponse;
import com.hekademos.hekademos_backend.entities.User;
import com.hekademos.hekademos_backend.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    public static class UpdateRoutineLinkRequest {
        public String routineLink;

        public String getRoutineLink() {
            return routineLink;
        }

        public void setRoutineLink(String routineLink) {
            this.routineLink = routineLink;
        }
    }

    @Autowired
    private IUserService userService;

    @PutMapping("/me/routine-link")
    public ResponseEntity<ApiResponse<User>> updateRoutineLink(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody UpdateRoutineLinkRequest body) {

        if (principal == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "No autenticado", null));
        }
        if (body == null || body.routineLink == null || body.routineLink.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "routineLink requerido", null));
        }

        String email = principal.getAttribute("email");
        User updated = userService.updateRoutineLink(email, body.routineLink);
        return ResponseEntity.ok(new ApiResponse<>(true, "Routine link actualizado", updated));
    }
}