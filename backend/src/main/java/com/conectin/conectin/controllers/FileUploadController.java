package com.conectin.conectin.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.conectin.conectin.config.JwtUtil;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/";
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/foto")
    public ResponseEntity<String> uploadFoto(@RequestParam("file") MultipartFile file,
                                            @RequestHeader("Authorization") String token) {
        try {
            // Validate JWT token
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Token inválido ou ausente");
            }
            String jwtToken = token.substring(7);
            String username = jwtUtil.extractUsername(jwtToken);
            if (!jwtUtil.validateToken(jwtToken, username)) {
                return ResponseEntity.status(401).body("Token expirado ou inválido");
            }

            if (file.isEmpty()) {
                return ResponseEntity.status(400).body("Arquivo vazio");
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao fazer upload: " + e.getMessage());
        }
    }
}