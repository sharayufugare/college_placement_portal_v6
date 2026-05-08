package com.example.placementportal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.placementportal.model.Student;
import com.example.placementportal.security.GoogleTokenVerifier;
import com.example.placementportal.security.JwtUtil;
import com.example.placementportal.service.StudentService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final StudentService studentService;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String email = req.getEmail() != null ? req.getEmail().toLowerCase().trim() : "";
        
        // Check domain restriction first
        if (!email.endsWith("@pccoer.in")) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Use official college email only!"));
        }
        
        // Try to login
        var studentOpt = studentService.login(email, req.getPassword());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password"));
        }
        
        Student student = studentOpt.get();
        String token = jwtUtil.generateToken(student.getEmail(), "STUDENT");

        return ResponseEntity.ok(new LoginResponse(student.getId(), token, student.getName(), student.getEmail()));
    }

    @PostMapping("/google")
    public LoginResponse loginWithGoogle(@RequestBody GoogleLoginRequest req) {
        GoogleTokenVerifier.GoogleTokenInfo tokenInfo = googleTokenVerifier.verifyIdToken(req.getIdToken());
        Student student = studentService.findByEmail(tokenInfo.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No registered student found for this college email"));

        String token = jwtUtil.generateToken(student.getEmail(), "STUDENT");
        return new LoginResponse(student.getId(), token, student.getName(), student.getEmail());
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class GoogleLoginRequest {
        private String idToken;
    }

    @Data
    public static class LoginResponse {
        private Long studentId;
        private String token;
        private String name;
        private String email;

        public LoginResponse(Long id, String token, String name, String email) {
            this.studentId = id;
            this.token = token;
            this.name = name;
            this.email = email;
        }
    }
    
    @Data
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
