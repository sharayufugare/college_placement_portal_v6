package com.example.placementportal.service;

import com.example.placementportal.model.Admin;
import com.example.placementportal.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<Admin> login(String username, String password) {
        if (username == null) return Optional.empty();
        String normalized = username.toLowerCase().trim();
        Optional<Admin> admin = adminRepository.findByUsername(normalized);
        if (admin.isEmpty() && normalized.contains("@")) {
            admin = adminRepository.findByEmail(normalized);
        }
        if (admin.isPresent() && passwordEncoder.matches(password, admin.get().getPassword())) {
            return admin;
        }
        return Optional.empty();
    }

    @Override
    public Admin register(Admin admin) {
        if (admin.getUsername() != null) admin.setUsername(admin.getUsername().toLowerCase().trim());
        if (admin.getEmail() != null) admin.setEmail(admin.getEmail().toLowerCase().trim());
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }
}

