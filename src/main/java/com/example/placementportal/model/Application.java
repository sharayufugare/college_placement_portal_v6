
package com.example.placementportal.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "company_id"})
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    private String status = "APPLIED";
    private String resultStatus = "Pending";
    private LocalDate appliedDate;

    public Application() {}

    public Application(Student student, Company company) {
        this.student = student;
        this.company = company;
        this.status = "APPLIED";
        this.resultStatus = "Pending";
        this.appliedDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Company getCompany() { return company; }
    public String getStatus() { return status; }
    public String getResultStatus() { return resultStatus; }
    public LocalDate getAppliedDate() { return appliedDate; }

    public void setStatus(String status) { this.status = status; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
}
