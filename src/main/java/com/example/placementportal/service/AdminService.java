package com.example.placementportal.service;

import com.example.placementportal.model.Admin;
import java.util.Optional;

public interface AdminService {
    Optional<Admin> login(String username, String password);
    Admin register(Admin admin);
}

