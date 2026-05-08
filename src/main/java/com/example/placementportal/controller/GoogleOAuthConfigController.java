package com.example.placementportal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth/google")
@CrossOrigin(origins = "*")
public class GoogleOAuthConfigController {

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    @GetMapping("/client-id")
    public Map<String, String> getClientId() {
        return Collections.singletonMap("clientId", googleClientId);
    }
}
