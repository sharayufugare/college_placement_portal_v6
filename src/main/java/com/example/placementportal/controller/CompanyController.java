package com.example.placementportal.controller;

import com.example.placementportal.model.Company;
import com.example.placementportal.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    private void mapCompanyFields(Company company, Map<String, String> payload) {
        company.setCompanyName(payload.getOrDefault("companyName", company.getCompanyName()));
        company.setSkills(payload.getOrDefault("skills", company.getSkills()));
        company.setCriteria(payload.getOrDefault("criteria", company.getCriteria()));
        company.setStatus(payload.getOrDefault("status", company.getStatus()));
        company.setBranchDepartment(payload.getOrDefault("branchDepartment", payload.getOrDefault("branch", company.getBranchDepartment())));
        company.setEligibleYear(payload.getOrDefault("eligibleYear", payload.getOrDefault("eligibleBatches", company.getEligibleYear())));
        company.setCourse(payload.getOrDefault("course", payload.getOrDefault("degree", company.getCourse())));
        company.setMinimumCgpaPercentage(payload.getOrDefault("minimumCgpaPercentage", company.getMinimumCgpaPercentage()));
        company.setInternshipTitleRole(payload.getOrDefault("internshipTitleRole", payload.getOrDefault("specification", company.getInternshipTitleRole())));
        company.setDuration(payload.getOrDefault("duration", payload.getOrDefault("internPeriod", company.getDuration())));
        company.setStipend(payload.getOrDefault("stipend", company.getStipend()));
        company.setLocation(payload.getOrDefault("location", company.getLocation()));
        company.setInternshipType(payload.getOrDefault("internshipType", payload.getOrDefault("workMode", company.getInternshipType())));
        company.setRegistrationDeadline(payload.getOrDefault("registrationDeadline", payload.getOrDefault("deadline", company.getRegistrationDeadline())));
        company.setInternshipStartDate(payload.getOrDefault("internshipStartDate", payload.getOrDefault("driveDate", company.getInternshipStartDate())));
        company.setInternshipEndDate(payload.getOrDefault("internshipEndDate", company.getInternshipEndDate()));
        company.setAptitudeTest(payload.getOrDefault("aptitudeTest", company.getAptitudeTest()));
        company.setTechnicalInterview(payload.getOrDefault("technicalInterview", company.getTechnicalInterview()));
        company.setHrInterview(payload.getOrDefault("hrInterview", company.getHrInterview()));
        company.setAssignmentProjectRound(payload.getOrDefault("assignmentProjectRound", company.getAssignmentProjectRound()));
        company.setResponsibilities(payload.getOrDefault("responsibilities", company.getResponsibilities()));
        company.setRequiredSkills(payload.getOrDefault("requiredSkills", company.getRequiredSkills()));
        company.setPreferredSkills(payload.getOrDefault("preferredSkills", company.getPreferredSkills()));
        company.setRegistrationLink(payload.getOrDefault("registrationLink", company.getRegistrationLink()));
        company.setCompanyCategory(payload.getOrDefault("companyCategory", company.getCompanyCategory()));
        String dreamCompany = payload.get("dreamCompany");
        if (dreamCompany != null) {
            company.setDreamCompany(Boolean.parseBoolean(dreamCompany));
        } else if (company.getCompanyCategory() != null) {
            company.setDreamCompany("dream".equalsIgnoreCase(company.getCompanyCategory().trim()));
        }
    }

    // ✅ 1. Add company
    @PostMapping("/add")
    public Company addCompany(@RequestBody Company company) {
        return companyRepository.save(company);
    }


    // ✅ 2. Get all companies
    @GetMapping("/all")
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    // ✅ 3. Search by skill or criteria
    @GetMapping("/search")
    public List<Company> searchCompany(@RequestParam(required = false) String skill,
                                       @RequestParam(required = false) String criteria) {
        String skillTerm = skill != null ? skill.trim() : null;
        String criteriaTerm = criteria != null ? criteria.trim() : null;
        if (skillTerm != null && criteriaTerm != null)
            return companyRepository.findBySkillsContainingIgnoreCaseAndCriteriaContainingIgnoreCase(skillTerm, criteriaTerm);
        else if (skillTerm != null)
            return companyRepository.findBySkillsContainingIgnoreCase(skillTerm);
        else if (criteriaTerm != null)
            return companyRepository.findByCriteriaContainingIgnoreCase(criteriaTerm);
        else
            return companyRepository.findAll();
    }

    // ✅ 4. Update company by ID
    @PutMapping("/update/{id}")
    public Company updateCompany(@PathVariable Long id, @RequestBody Company updatedCompany) {
        Optional<Company> existing = companyRepository.findById(id);
        if (existing.isPresent()) {
            Company company = existing.get();
            company.setCompanyName(updatedCompany.getCompanyName());
            company.setSkills(updatedCompany.getSkills());
            company.setCriteria(updatedCompany.getCriteria());
            company.setStatus(updatedCompany.getStatus());
            company.setBranchDepartment(updatedCompany.getBranchDepartment());
            company.setEligibleYear(updatedCompany.getEligibleYear());
            company.setCourse(updatedCompany.getCourse());
            company.setMinimumCgpaPercentage(updatedCompany.getMinimumCgpaPercentage());
            company.setInternshipTitleRole(updatedCompany.getInternshipTitleRole());
            company.setDuration(updatedCompany.getDuration());
            company.setStipend(updatedCompany.getStipend());
            company.setLocation(updatedCompany.getLocation());
            company.setInternshipType(updatedCompany.getInternshipType());
            company.setRegistrationDeadline(updatedCompany.getRegistrationDeadline());
            company.setInternshipStartDate(updatedCompany.getInternshipStartDate());
            company.setInternshipEndDate(updatedCompany.getInternshipEndDate());
            company.setAptitudeTest(updatedCompany.getAptitudeTest());
            company.setTechnicalInterview(updatedCompany.getTechnicalInterview());
            company.setHrInterview(updatedCompany.getHrInterview());
            company.setAssignmentProjectRound(updatedCompany.getAssignmentProjectRound());
            company.setResponsibilities(updatedCompany.getResponsibilities());
            company.setRequiredSkills(updatedCompany.getRequiredSkills());
            company.setPreferredSkills(updatedCompany.getPreferredSkills());
            company.setRegistrationLink(updatedCompany.getRegistrationLink());
            company.setCompanyCategory(updatedCompany.getCompanyCategory());
            company.setDreamCompany(updatedCompany.getDreamCompany());
            return companyRepository.save(company);
        }
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Company not found with id: " + id);
    }

    // ✅ 5. Delete company by ID
    @DeleteMapping("/delete/{id}")
    public String deleteCompany(@PathVariable Long id) {
        if (!companyRepository.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
        return "Company deleted with ID: " + id;
    }

    // ✅ 6. Get company by ID
    @GetMapping("/{id}")
    public Company getCompanyById(@PathVariable Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Company not found with id: " + id));
    }
}



