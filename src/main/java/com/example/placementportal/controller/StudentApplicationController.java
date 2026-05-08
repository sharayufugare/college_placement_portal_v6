package com.example.placementportal.controller;

import com.example.placementportal.model.*;
import com.example.placementportal.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*")
public class StudentApplicationController {

    private final StudentRepository studentRepo;
    private final CompanyRepository companyRepo;
    private final ApplicationRepository applicationRepo;

    public StudentApplicationController(
            StudentRepository studentRepo,
            CompanyRepository companyRepo,
            ApplicationRepository applicationRepo
    ) {
        this.studentRepo = studentRepo;
        this.companyRepo = companyRepo;
        this.applicationRepo = applicationRepo;
    }

    @PostMapping("/apply/{companyId}")
    public ResponseEntity<?> applyToCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false) Long studentId
    ) {
        System.out.println("[StudentApplicationController] Apply attempt: companyId=" + companyId + ", studentId=" + studentId);
        if (studentId == null) {
            return ResponseEntity.badRequest().body("Student ID is missing. Please log in again.");
        }
        
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        boolean dreamCompany = Boolean.TRUE.equals(company.getDreamCompany())
                || (company.getCompanyCategory() != null && "dream".equalsIgnoreCase(company.getCompanyCategory()));
        boolean blockedForInternship = Boolean.TRUE.equals(student.getInternshipSecured()) && !dreamCompany;
        boolean blockedByAdmin = Boolean.TRUE.equals(student.getBlockedByAdmin()) && !Boolean.TRUE.equals(student.getManualUnblockOverride());
        if (blockedByAdmin || blockedForInternship) {
            String reason = blockedForInternship
                    ? "Application blocked: Internship already secured. Dream companies are still allowed."
                    : "Application blocked by TNP admin";
            return ResponseEntity.badRequest().body(reason);
        }

        if (applicationRepo.findByStudentIdAndCompanyId(studentId, companyId).isPresent()) {
            return ResponseEntity.badRequest().body("Already applied");
        }

        Application application = new Application(student, company);
        applicationRepo.save(application);

        return ResponseEntity.ok("Applied successfully");
    }
}

