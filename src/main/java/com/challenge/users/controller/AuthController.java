package com.challenge.users.controller;

import com.challenge.users.dto.AuthRequest;
import com.challenge.users.dto.AuthResponse;
import com.challenge.users.entity.User;
import com.challenge.users.repository.UserRepository;
import com.challenge.users.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Authentifier un utilisateur et obtenir un token JWT")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
