package com.example.placementportal.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class GoogleTokenVerifier {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleTokenInfo verifyIdToken(String idToken) {
        try {
            String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_INFO_URL + encodedToken))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Invalid Google ID token");
            }

            JsonNode root = objectMapper.readTree(response.body());
            boolean emailVerified = root.path("email_verified").asText("false").equalsIgnoreCase("true");
            if (!emailVerified) {
                throw new RuntimeException("Google account email is not verified");
            }

            String email = root.path("email").asText(null);
            if (email == null || !email.toLowerCase(Locale.ROOT).endsWith("@pccoer.in")) {
                throw new RuntimeException("Use official college email only");
            }

            String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
            String name = root.path("name").asText("Unknown");
            return new GoogleTokenInfo(normalizedEmail, name);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Unable to verify Google token", ex);
        }
    }

    public static record GoogleTokenInfo(String email, String name) {
    }
}
