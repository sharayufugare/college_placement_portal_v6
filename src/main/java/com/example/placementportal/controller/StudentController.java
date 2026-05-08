package com.example.placementportal.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.placementportal.dto.ApplicationDTO;
import com.example.placementportal.model.Company;
import com.example.placementportal.model.Student;
import com.example.placementportal.repository.ApplicationRepository;
import com.example.placementportal.repository.CompanyRepository;
import com.example.placementportal.repository.StudentRepository;
import com.example.placementportal.service.StudentService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository repo;

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public Student register(@RequestBody Student s) {
        try {
            return studentService.createStudent(s);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to register student");
        }
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public Student login(@RequestBody Student request) {
        String email = request.getEmail() != null ? request.getEmail().toLowerCase().trim() : "";
        Optional<Student> s = studentService.login(email, request.getPassword());
        return s.orElse(null);
    }

    // ---------------- DASHBOARD ----------------
    @GetMapping("/dashboard/{id}")
    public ResponseEntity<Student> dashboard(@PathVariable Long id) {
        System.out.println("[StudentController] Fetching dashboard for ID: " + id);
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    System.err.println("[StudentController] Student not found for ID: " + id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/all")
    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    // ---------------- UPDATE PROFILE ----------------
    @PutMapping("/update/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student updated) {
        System.out.println("[StudentController] Updating student " + id + " with payload: " + updated);
        try {
            Student result = studentService.updateProfile(id, updated);
            System.out.println("[StudentController] Successfully updated student " + id);
            return result;
        } catch (Exception e) {
            System.err.println("[StudentController] Error updating student " + id + ": " + e.getMessage());
            throw e;
        }
    }

    // ---------------- UPLOAD RESUME ----------------
    @PostMapping("/uploadResume/{id}")
    public Student uploadResume(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "resumes" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        
        String fileName = id + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);
        return studentService.saveResume(id, "/uploads/resumes/" + fileName);
    }

    // ---------------- UPLOAD PHOTO ----------------
    @PostMapping("/uploadPhoto/{id}")
    public Student uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "photos" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = id + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);
        return studentService.savePhoto(id, "/uploads/photos/" + fileName);
    }

    // ---------------- UPLOAD CERTIFICATE ----------------
    @PostMapping("/uploadCertificate/{id}")
    public Student uploadCertificate(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "certificates" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = id + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);
        return studentService.saveCertificate(id, "/uploads/certificates/" + fileName);
    }

    // ---------------- VIEW ELIGIBLE COMPANIES ----------------
    @GetMapping("/eligible/{studentId}")
    public List<Company> getEligibleCompanies(@PathVariable Long studentId) {
        Optional<Student> studentOpt = repo.findById(studentId);
        if (studentOpt.isEmpty()) {
            return new ArrayList<>(); // Return empty if student not found
        }
        Student student = studentOpt.get();
        List<Company> companies = companyRepository.findAll();
        List<Company> eligibleCompanies = new ArrayList<>();

        double studentCgpa = parseCgpa(student.getCgpa());
        String[] studentSkills = student.getSkills() == null ? new String[0] : student.getSkills().split(",");

        for (Company company : companies) {
            boolean eligibleByCgpa = true;
            double minCgpa = parseCgpa(company.getCriteria());
            if (minCgpa > 0 && studentCgpa < minCgpa) {
                eligibleByCgpa = false;
            }

            boolean hasRequiredSkill = false;
            String[] requiredSkills = company.getSkills() == null ? new String[0] : company.getSkills().split(",");
            
            if (requiredSkills.length == 0) {
                hasRequiredSkill = true; // No skills required
            } else {
                // Relaxed logic: At least one skill should match
                for (String reqSkill : requiredSkills) {
                    for (String studSkill : studentSkills) {
                        if (studSkill.trim().equalsIgnoreCase(reqSkill.trim())) {
                            hasRequiredSkill = true;
                            break;
                        }
                    }
                    if (hasRequiredSkill) break;
                }
            }

            if (eligibleByCgpa && hasRequiredSkill) {
                eligibleCompanies.add(company);
            }
        }

        return eligibleCompanies;
    }

    private double parseCgpa(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

  @GetMapping("/applications/{studentId}")
  public List<ApplicationDTO> getMyApplications(@PathVariable Long studentId) {
      return applicationRepository.findByStudentId(studentId)
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

}
