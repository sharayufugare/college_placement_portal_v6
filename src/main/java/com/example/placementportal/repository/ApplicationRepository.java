package com.example.placementportal.repository;

import com.example.placementportal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByStudentIdAndCompanyId(Long studentId, Long companyId);
    List<Application> findByCompanyId(Long companyId);
    List<Application> findByStudentId(Long studentId);
}