package com.example.placementportal.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String prn;

    @Column(nullable = false)
    private String role = "STUDENT";

    @JsonAlias("fullName")
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String skills;
    private String criteria;
    private String cgpa;
    private String branch;
    @Column(name = "`year`")
    private String year;
    private String phone;
    private String dob;
    private String linkedin;
    private String github;
    private String gender;
    private String achievements;
    private String tenthPercentage;
    private String twelfthOrDiplomaPercentage;
    private String beBtechPercentage;
    private String degree;
    private String specialization;
    private String graduationYear;
    private String collegeName;
    private String technicalCourseCertification;
    private String certificationAgency;
    private String certificationDurationHours;
    private String certificatePath;
    private String activeBacklog;
    private String codechefRating;
    private String codechefLink;
    private String hackerrankRating;
    private String hackerrankLink;
    private String leetcodeScore;
    private String leetcodeLink;
    private String cocubeScore;
    private String linkedinLink;
    private String technicalAchievement;
    private String personalAchievement;
    private String projectDetails;
    private Boolean blockedByAdmin = false;
    private String blockReason;
    private Boolean internshipSecured = false;
    private Boolean manualUnblockOverride = false;

    private String resumePath;     // stored locally in /uploads/resumes
    private String photoPath;      // stored locally in /uploads/photos
    public String getName() {
        return name;
    }

}


