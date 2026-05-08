package com.example.placementportal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.placementportal.model.Admin;
import com.example.placementportal.model.Application;
import com.example.placementportal.model.Student;
import com.example.placementportal.repository.AdminRepository;
import com.example.placementportal.repository.ApplicationRepository;
import com.example.placementportal.repository.StudentRepository;
import com.example.placementportal.security.GoogleTokenVerifier;
import com.example.placementportal.security.JwtUtil;
import com.example.placementportal.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;

    @GetMapping("/applications/{companyId}")
    public List<Application> getApplicationsForCompany(@PathVariable Long companyId) {
        return applicationRepository.findByCompanyId(companyId);
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/coordinators")
    public List<CoordinatorInfo> getCoordinators() {
        return adminRepository.findAll().stream()
                .filter(admin -> admin.getRole() != null && admin.getRole().toLowerCase().contains("coordinator"))
                .map(admin -> new CoordinatorInfo(
                        admin.getId(),
                        admin.getUsername(),
                        admin.getEmail(),
                        admin.getFullName(),
                        admin.getDepartment(),
                        admin.getRole()
                ))
                .toList();
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Admin admin) {
        Map<String, Object> response = new HashMap<>();

        if (admin.getUsername() != null) {
            admin.setUsername(admin.getUsername().toLowerCase().trim());
        }
        if (admin.getEmail() != null) {
            admin.setEmail(admin.getEmail().toLowerCase().trim());
        }

        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            response.put("status", "failed");
            response.put("message", "Username is required");
            return response;
        }
        if (admin.getPassword() == null || admin.getPassword().trim().length() < 6) {
            response.put("status", "failed");
            response.put("message", "Password is required and must be at least 6 characters");
            return response;
        }
        if (admin.getEmail() == null || !admin.getEmail().trim().toLowerCase().endsWith("@pccoer.in")) {
            response.put("status", "failed");
            response.put("message", "Please use an official @pccoer.in email address");
            return response;
        }
        if (admin.getFullName() == null || admin.getFullName().trim().length() < 3) {
            response.put("status", "failed");
            response.put("message", "Full name is required");
            return response;
        }
        if (admin.getDepartment() == null || admin.getDepartment().trim().isEmpty()) {
            response.put("status", "failed");
            response.put("message", "Department is required");
            return response;
        }
        if (admin.getRole() == null || admin.getRole().trim().isEmpty()) {
            response.put("status", "failed");
            response.put("message", "Role is required");
            return response;
        }
        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            response.put("status", "failed");
            response.put("message", "Username already exists");
            return response;
        }
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            response.put("status", "failed");
            response.put("message", "Email already exists");
            return response;
        }

        Admin savedAdmin;
        try {
            savedAdmin = adminService.register(admin);
        } catch (DataIntegrityViolationException e) {
            response.put("status", "failed");
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("department")) {
                response.put("message", "An admin for this department already exists. Each department can have only one coordinator. Please use a different department or contact the TNP head.");
            } else if (msg.contains("username")) {
                response.put("message", "Username already exists");
            } else if (msg.contains("email")) {
                response.put("message", "Email already exists");
            } else {
                response.put("message", "Registration failed due to a conflict. Please check your details and try again.");
            }
            return response;
        }
        response.put("status", "success");
        response.put("admin", savedAdmin);
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {
        String username = req.get("username") != null ? req.get("username").toLowerCase().trim() : "";
        String password = req.get("password");

        Map<String, Object> response = new HashMap<>();
        if (username.contains("@") && !username.endsWith("@pccoer.in")) {
            response.put("status", "failed");
            response.put("message", "Please use your official college email address ending with @pccoer.in");
            return response;
        }

        Optional<Admin> adminOpt = adminService.login(username, password);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            response.put("status", "success");
            response.put("admin", admin);
            String subject = admin.getEmail() != null ? admin.getEmail() : admin.getUsername();
            response.put("token", jwtUtil.generateToken(subject, "ADMIN"));
        } else {
            response.put("status", "failed");
            response.put("message", "Invalid credentials");
        }

        return response;
    }

    @PostMapping("/google-login")
    public Map<String, Object> loginWithGoogle(@RequestBody GoogleLoginRequest req) {
        Map<String, Object> response = new HashMap<>();
        GoogleTokenVerifier.GoogleTokenInfo tokenInfo = googleTokenVerifier.verifyIdToken(req.getIdToken());
        String email = tokenInfo.email() != null ? tokenInfo.email().toLowerCase().trim() : "";

        if (email.isEmpty() || !email.endsWith("@pccoer.in")) {
            response.put("status", "failed");
            response.put("message", "Please sign in with your official college email address ending with @pccoer.in");
            return response;
        }

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isEmpty()) {
            adminOpt = adminRepository.findByUsername(email);
        }

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            response.put("status", "success");
            response.put("admin", admin);
            response.put("token", jwtUtil.generateToken(email, "ADMIN"));
        } else {
            response.put("status", "failed");
            response.put("message", "No admin account found for this college email");
        }

        return response;
    }

    public static class GoogleLoginRequest {
        private String idToken;

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }

    public static record CoordinatorInfo(Long id, String username, String email, String fullName, String department, String role) {}
}
