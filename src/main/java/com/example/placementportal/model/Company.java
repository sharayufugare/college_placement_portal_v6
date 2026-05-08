package com.example.placementportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "skills")
    private String skills;

    @Column(name = "criteria")
    private String criteria;

    @Column(name = "status")
    private String status;

    private String branchDepartment;
    private String eligibleYear;
    private String course;
    private String minimumCgpaPercentage;
    private String internshipTitleRole;
    private String duration;
    private String stipend;
    private String location;
    private String internshipType;
    private String registrationDeadline;
    private String internshipStartDate;
    private String internshipEndDate;
    private String aptitudeTest;
    private String technicalInterview;
    private String hrInterview;
    private String assignmentProjectRound;
    @Column(length = 2000)
    private String responsibilities;
    private String requiredSkills;
    private String preferredSkills;
    private String registrationLink;
    private String companyCategory;
    private Boolean dreamCompany = false;

    public Company() {}
}
