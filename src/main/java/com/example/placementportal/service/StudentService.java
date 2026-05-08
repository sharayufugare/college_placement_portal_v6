/*package com.example.placementportal.service;

import com.example.placementportal.model.Student;
import java.util.Optional;
import java.util.List;

public interface StudentService {
    Student registerStudent(Student student);
    Optional<Student> loginStudent(String email, String password);
    Optional<Student> getStudentById(Long id);
    List<Student> getAllStudents();
    Student updateStudent(Long id, Student updatedStudent);
}
*/
package com.example.placementportal.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.placementportal.model.Student;
import com.example.placementportal.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repo;
    private final PasswordEncoder encoder;

    public Student createStudent(Student s) {
        if (!StringUtils.hasText(s.getPrn())) {
            throw new IllegalArgumentException("PRN is required");
        }
        if (!StringUtils.hasText(s.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        s.setEmail(s.getEmail().toLowerCase().trim());
        if (!s.getEmail().endsWith("@pccoer.in")) {
            throw new IllegalArgumentException("Use official college email only (@pccoer.in)");
        }
        if (!StringUtils.hasText(s.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
        if (repo.findByPrn(s.getPrn()).isPresent()) {
            throw new IllegalArgumentException("PRN already exists");
        }
        if (repo.findByEmail(s.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!StringUtils.hasText(s.getRole())) {
            s.setRole("STUDENT");
        }
        s.setPassword(encoder.encode(s.getPassword()));
        return repo.save(s);
    }

    public Optional<Student> login(String email, String password) {
        if (email == null) return Optional.empty();
        Optional<Student> s = repo.findByEmail(email.toLowerCase().trim());
        if (s.isPresent() && encoder.matches(password, s.get().getPassword())) {
            return s;
        }
        return Optional.empty();
    }

    public Optional<Student> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return repo.findByEmail(email.toLowerCase().trim());
    }

    public Student updateProfile(Long id, Student updated) {
        Student s = repo.findById(id).orElseThrow(() -> new RuntimeException("Student not found for update: " + id));
        System.out.println("[StudentService] Starting update for student: " + s.getEmail());

        if (updated.getName() != null) { System.out.println("Updating name: " + updated.getName()); s.setName(updated.getName()); }
        if (updated.getSkills() != null) { System.out.println("Updating skills: " + updated.getSkills()); s.setSkills(updated.getSkills()); }
        if (updated.getCriteria() != null) s.setCriteria(updated.getCriteria());
        if (updated.getCgpa() != null) { System.out.println("Updating cgpa: " + updated.getCgpa()); s.setCgpa(updated.getCgpa()); }
        if (updated.getBranch() != null) s.setBranch(updated.getBranch());
        if (updated.getYear() != null) s.setYear(updated.getYear());
        if (updated.getPhone() != null) s.setPhone(updated.getPhone());
        if (updated.getDob() != null) s.setDob(updated.getDob());
        if (updated.getLinkedin() != null) s.setLinkedin(updated.getLinkedin());
        if (updated.getGithub() != null) s.setGithub(updated.getGithub());
        if (updated.getGender() != null) s.setGender(updated.getGender());
        if (updated.getAchievements() != null) s.setAchievements(updated.getAchievements());
        if (updated.getTenthPercentage() != null) s.setTenthPercentage(updated.getTenthPercentage());
        if (updated.getTwelfthOrDiplomaPercentage() != null) s.setTwelfthOrDiplomaPercentage(updated.getTwelfthOrDiplomaPercentage());
        if (updated.getBeBtechPercentage() != null) s.setBeBtechPercentage(updated.getBeBtechPercentage());
        if (updated.getDegree() != null) s.setDegree(updated.getDegree());
        if (updated.getSpecialization() != null) s.setSpecialization(updated.getSpecialization());
        if (updated.getGraduationYear() != null) s.setGraduationYear(updated.getGraduationYear());
        if (updated.getCollegeName() != null) s.setCollegeName(updated.getCollegeName());
        if (updated.getTechnicalCourseCertification() != null) s.setTechnicalCourseCertification(updated.getTechnicalCourseCertification());
        if (updated.getCertificationAgency() != null) s.setCertificationAgency(updated.getCertificationAgency());
        if (updated.getCertificationDurationHours() != null) s.setCertificationDurationHours(updated.getCertificationDurationHours());
        if (updated.getActiveBacklog() != null) s.setActiveBacklog(updated.getActiveBacklog());
        if (updated.getCodechefRating() != null) s.setCodechefRating(updated.getCodechefRating());
        if (updated.getCodechefLink() != null) s.setCodechefLink(updated.getCodechefLink());
        if (updated.getHackerrankRating() != null) s.setHackerrankRating(updated.getHackerrankRating());
        if (updated.getHackerrankLink() != null) s.setHackerrankLink(updated.getHackerrankLink());
        if (updated.getLeetcodeScore() != null) s.setLeetcodeScore(updated.getLeetcodeScore());
        if (updated.getLeetcodeLink() != null) s.setLeetcodeLink(updated.getLeetcodeLink());
        if (updated.getCocubeScore() != null) s.setCocubeScore(updated.getCocubeScore());
        if (updated.getLinkedinLink() != null) s.setLinkedinLink(updated.getLinkedinLink());
        if (updated.getTechnicalAchievement() != null) s.setTechnicalAchievement(updated.getTechnicalAchievement());
        if (updated.getPersonalAchievement() != null) s.setPersonalAchievement(updated.getPersonalAchievement());
        if (updated.getProjectDetails() != null) s.setProjectDetails(updated.getProjectDetails());

        Student saved = repo.save(s);
        System.out.println("[StudentService] Save completed for student: " + saved.getEmail());
        return saved;
    }

    public Student saveResume(Long id, String path) {
        Student s = repo.findById(id).orElseThrow();
        s.setResumePath(path);
        return repo.save(s);
    }

    public Student savePhoto(Long id, String path) {
        Student s = repo.findById(id).orElseThrow();
        s.setPhotoPath(path);
        return repo.save(s);
    }

    public Student saveCertificate(Long id, String path) {
        Student s = repo.findById(id).orElseThrow();
        s.setCertificatePath(path);
        return repo.save(s);
    }
}
