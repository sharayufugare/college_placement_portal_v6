package com.example.placementportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs once on startup to fix any legacy database constraints.
 *
 * BUG FIX: The 'admins' table previously had a UNIQUE constraint on the
 * 'department' column (UKmi8vkhus4xbdbqcac2jm4spvd), which prevented
 * registering more than one admin per department.
 * This migration drops that constraint if it still exists.
 */
@Component
public class DatabaseMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        dropDepartmentUniqueConstraintIfExists();
    }

    private void dropDepartmentUniqueConstraintIfExists() {
        try {
            // Check if the constraint exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'admins' " +
                "AND CONSTRAINT_TYPE = 'UNIQUE' " +
                "AND CONSTRAINT_NAME = 'UKmi8vkhus4xbdbqcac2jm4spvd'",
                Integer.class
            );

            if (count != null && count > 0) {
                jdbcTemplate.execute("ALTER TABLE admins DROP INDEX UKmi8vkhus4xbdbqcac2jm4spvd");
                log.info("✅ DATABASE FIX APPLIED: Dropped unique constraint on admins.department. Multiple admins per department are now allowed.");
            } else {
                log.info("✅ admins.department constraint check: No legacy unique constraint found. Nothing to fix.");
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not check/drop department unique constraint (may be normal if using H2 or constraint already gone): {}", e.getMessage());
        }
    }
}
