/*package com.example.placementportal.controller;

import com.example.placementportal.model.Application;
import com.example.placementportal.repository.ApplicationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tnp")
@CrossOrigin
public class TnpApiController {

    private final ApplicationRepository applicationRepo;

    public TnpApiController(ApplicationRepository applicationRepo) {
        this.applicationRepo = applicationRepo;
    }

    // 🔹 All applications
    @GetMapping("/applications")
    public List<Application> getAllApplications() {
        return applicationRepo.findAll();
    }

    // 🔹 Company wise
    @GetMapping("/applications/company/{companyId}")
    public List<Application> getByCompany(@PathVariable Long companyId) {
        return applicationRepo.findByCompanyId(companyId);
    }
}
*/
package com.example.placementportal.controller;

import com.example.placementportal.dto.ApplicationDTO;
import com.example.placementportal.model.Application;
import com.example.placementportal.model.Student;
import com.example.placementportal.repository.ApplicationRepository;
import com.example.placementportal.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tnp")
@CrossOrigin(origins = "*")
public class TnpApiController {

    private final ApplicationRepository applicationRepo;
    private final StudentRepository studentRepository;

    public TnpApiController(ApplicationRepository applicationRepo, StudentRepository studentRepository) {
        this.applicationRepo = applicationRepo;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/applications")
    public List<ApplicationDTO> getAllApplications() {
        return applicationRepo.findAll()
                .stream()
                .map(app -> new ApplicationDTO(
                        app.getId(),
                        app.getStudent().getName(),
                        app.getCompany().getCompanyName(),
                        app.getStatus(),
                        app.getResultStatus(),
                        app.getAppliedDate()
                ))
                .toList();
    }

    @GetMapping("/applications/summary")
    public AnalyticsSummary getApplicationsSummary() {
        List<Application> applications = applicationRepo.findAll();

        long totalStudents = studentRepository.count();

        long totalPlaced = applications.stream()
                .filter(app -> isPlaced(app.getResultStatus()))
                .count();

        long totalInternships = applications.stream()
                .filter(app -> isInternship(app.getResultStatus()))
                .count();

        double placementRate = totalStudents > 0 ? (totalPlaced * 100.0 / totalStudents) : 0.0;

        Map<String, Long> placementsByYear = applications.stream()
                .filter(app -> isPlaced(app.getResultStatus()))
                .filter(app -> app.getAppliedDate() != null)
                .collect(Collectors.groupingBy(app -> String.valueOf(app.getAppliedDate().getYear()), Collectors.counting()));

        Map<String, Long> internshipsByYear = applications.stream()
                .filter(app -> isInternship(app.getResultStatus()))
                .filter(app -> app.getAppliedDate() != null)
                .collect(Collectors.groupingBy(app -> String.valueOf(app.getAppliedDate().getYear()), Collectors.counting()));

        Map<String, Long> studentsByYear = applications.stream()
                .filter(app -> app.getAppliedDate() != null)
                .collect(Collectors.groupingBy(app -> String.valueOf(app.getAppliedDate().getYear()),
                        Collectors.mapping(app -> app.getStudent().getId(), Collectors.toSet())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (long) entry.getValue().size()));

        List<YearlyCount> placementTrends = placementsByYear.entrySet().stream()
                .map(e -> new YearlyCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.year().compareTo(b.year()))
                .toList();

        List<YearlyCount> internshipTrends = internshipsByYear.entrySet().stream()
                .map(e -> new YearlyCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.year().compareTo(b.year()))
                .toList();

        List<YearlyCount> studentsTrends = studentsByYear.entrySet().stream()
                .map(e -> new YearlyCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.year().compareTo(b.year()))
                .toList();

        List<TopCompany> topCompanies = applications.stream()
                .filter(app -> isPlaced(app.getResultStatus()))
                .collect(Collectors.groupingBy(app -> app.getCompany().getCompanyName(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new TopCompany(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.placedCount(), a.placedCount()))
                .limit(6)
                .toList();

        return new AnalyticsSummary(totalStudents, totalPlaced, totalInternships, placementRate, placementTrends, internshipTrends, studentsTrends, topCompanies);
    }

    private boolean isPlaced(String status) {
        return status != null && (status.equalsIgnoreCase("accepted") || status.equalsIgnoreCase("placed") || status.equalsIgnoreCase("offer") || status.equalsIgnoreCase("selected"));
    }

    private boolean isInternship(String status) {
        return status != null && status.toLowerCase().contains("intern");
    }

    public static record AnalyticsSummary(
            long totalStudents,
            long totalPlaced,
            long totalInternships,
            double placementRate,
            List<YearlyCount> placementTrends,
            List<YearlyCount> internshipTrends,
            List<YearlyCount> studentsTrends,
            List<TopCompany> topCompanies
    ) {}

    public static record YearlyCount(String year, long value) {}

    public static record TopCompany(String companyName, long placedCount) {}

    @PutMapping("/applications/{id}/accept")
    public ResponseEntity<String> acceptApplication(@PathVariable Long id) {
        Application app = applicationRepo.findById(id).orElseThrow();
        app.setResultStatus("Selected");
        applicationRepo.save(app);
        return ResponseEntity.ok("Application Selected");
    }

    @PutMapping("/applications/{id}/reject")
    public ResponseEntity<String> rejectApplication(@PathVariable Long id) {
        Application app = applicationRepo.findById(id).orElseThrow();
        app.setResultStatus("Rejected");
        applicationRepo.save(app);
        return ResponseEntity.ok("Application Rejected");
    }

    @PutMapping("/applications/{id}/placed")
    public ResponseEntity<String> markAsPlaced(@PathVariable Long id) {
        Application app = applicationRepo.findById(id).orElseThrow();
        app.setResultStatus("Selected");
        applicationRepo.save(app);
        
        Student s = app.getStudent();
        if (s != null) {
            String duration = app.getCompany().getDuration();
            s.setInternshipSecured(true);
            s.setBlockedByAdmin(true);
            s.setBlockReason("Placed at " + app.getCompany().getCompanyName() + " (Duration: " + (duration != null ? duration : "N/A") + ")");
            studentRepository.save(s);
        }
        
        return ResponseEntity.ok("Marked as Placed");
    }

    @PutMapping("/applications/{id}/shortlist")
    public ResponseEntity<String> shortlistApplication(@PathVariable Long id) {
        Application app = applicationRepo.findById(id).orElseThrow();
        app.setResultStatus("Shortlisted");
        applicationRepo.save(app);
        return ResponseEntity.ok("Student Shortlisted");
    }

    @PutMapping("/applications/{id}/select")
    public ResponseEntity<String> selectApplication(@PathVariable Long id) {
        Application app = applicationRepo.findById(id).orElseThrow();
        app.setResultStatus("Selected");
        applicationRepo.save(app);

        Student s = app.getStudent();
        if (s != null) {
            String duration = app.getCompany().getDuration();
            s.setInternshipSecured(true);
            s.setBlockedByAdmin(true);
            s.setBlockReason("Selected by " + app.getCompany().getCompanyName() + " (Duration: " + (duration != null ? duration : "N/A") + ")");
            studentRepository.save(s);
        }

        return ResponseEntity.ok("Student Selected");
    }

    // --- Placement & Student Management ---

    public static record PlacementDTO(
            Long studentId,
            String prn,
            String studentName,
            String companyName,
            String duration,
            String role,
            String stipend,
            Boolean isBlocked,
            String resultStatus
    ) {}

    @GetMapping("/placements")
    public List<PlacementDTO> getPlacements() {
        return applicationRepo.findAll().stream()
                .filter(app -> "Selected".equalsIgnoreCase(app.getResultStatus()) || "Placed".equalsIgnoreCase(app.getResultStatus()))
                .map(app -> new PlacementDTO(
                        app.getStudent().getId(),
                        app.getStudent().getPrn(),
                        app.getStudent().getName(),
                        app.getCompany().getCompanyName(),
                        app.getCompany().getDuration(),
                        app.getCompany().getInternshipTitleRole(),
                        app.getCompany().getStipend(),
                        app.getStudent().getBlockedByAdmin(),
                        app.getResultStatus()
                ))
                .toList();
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PostMapping("/students/{id}/block")
    public ResponseEntity<String> blockStudent(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Student s = studentRepository.findById(id).orElseThrow();
        s.setBlockedByAdmin(true);
        s.setManualUnblockOverride(false);
        s.setBlockReason(body.getOrDefault("reason", "Blocked by Admin"));
        studentRepository.save(s);
        return ResponseEntity.ok("Student Blocked");
    }

    @PostMapping("/students/{id}/unblock")
    public ResponseEntity<String> unblockStudent(@PathVariable Long id) {
        Student s = studentRepository.findById(id).orElseThrow();
        s.setBlockedByAdmin(false);
        s.setManualUnblockOverride(true);
        s.setBlockReason(null);
        studentRepository.save(s);
        return ResponseEntity.ok("Student Unblocked");
    }
}

