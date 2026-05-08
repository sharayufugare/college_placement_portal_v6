package com.example.placementportal.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class StaticPageAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public StaticPageAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Add CORS headers to all responses
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");

        // Allow OPTIONS requests for CORS preflight
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        boolean studentPage = "/student_dashboard.html".equals(path);
        boolean studentApi = path.startsWith("/student/")
                && !path.equals("/student/register")
                && !path.equals("/student/login")
                && !path.equals("/student/all");
        boolean adminPage = "/tnp_dashboard.html".equals(path) || "/analytics.html".equals(path);
        boolean adminApi = (path.startsWith("/api/company/")
                && !(request.getMethod().equalsIgnoreCase("GET")
                && (path.equals("/api/company/all") || path.equals("/api/company/search"))))
                || path.startsWith("/tnp/");

        if (studentPage || studentApi || adminPage || adminApi) {
            System.out.println("[AuthFilter] Authenticating path: " + path + " (Admin: " + (adminPage || adminApi) + ")");

            boolean requiresAdmin = adminPage || adminApi;
            String tokenName = requiresAdmin ? "tnp_admin_token" : "studentToken";
            String token = getCookieValue(request, tokenName);

            if (!isValidToken(token, requiresAdmin)) {
                System.out.println("[AuthFilter] Invalid token for path: " + path);
                
                // For API calls, return 401 instead of redirecting
                if (studentApi || adminApi) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"status\":\"failed\",\"message\":\"Unauthorized. Please log in again.\"}");
                    response.getWriter().flush();
                } else if (adminPage) {
                    response.sendRedirect("/tnp_admin_login.html");
                } else {
                    response.sendRedirect("/student_login.html");
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        // 1. Try Authorization: Bearer <token> header first (used by fetch calls)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 2. Fall back to cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        Optional<Cookie> cookie = Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .findFirst();
        return cookie.map(Cookie::getValue).orElse(null);
    }

    private boolean isValidToken(String token, boolean requiresAdmin) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            String role = jwtUtil.extractRole(token);
            if (requiresAdmin) {
                return "ADMIN".equals(role);
            } else {
                return "STUDENT".equals(role) || "ADMIN".equals(role); // Admins can access student pages
            }
        } catch (Exception e) {
            return false;
        }
    }
}
