package com.hekademos.hekademos_backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import com.hekademos.hekademos_backend.services.IUserService;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final IUserService userService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public OAuth2SuccessHandler(JwtService jwtService, IUserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        // Datos del usuario de Google
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();
        if (name == null || name.isBlank()) {
            name = oidcUser.getGivenName();
        }

        // Persistir/actualizar usuario si no existe
        userService.saveIfNotExists(email, name, picture);

        // Generar JWT y redirigir al frontend
        String token = jwtService.generateToken(email, name, picture);
        String redirect = String.format("%s/oauth2/callback?token=%s", frontendUrl, token);
        response.sendRedirect(redirect);
    }
}