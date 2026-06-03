package com.challenge.users.controller;

import com.challenge.users.dto.BatchImportResult;
import com.challenge.users.dto.UserDto;
import com.challenge.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @GetMapping("/generate")
    @Operation(summary = "Générer des utilisateurs fictifs et télécharger le fichier JSON")
    public ResponseEntity<byte[]> generateUsers(@RequestParam int count) throws Exception {
        List<UserDto> users = userService.generateUsers(count);
        byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(users);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.json\"")
                .body(json);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer un fichier JSON d'utilisateurs en base de données")
    public ResponseEntity<BatchImportResult> batchImport(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(userService.batchImport(file));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Consulter mon profil",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.findByUsername(userDetails.getUsername()));
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Consulter le profil d'un utilisateur (rôle admin requis)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDto> getUserByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Accès refusé : rôle admin requis");
        }

        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
