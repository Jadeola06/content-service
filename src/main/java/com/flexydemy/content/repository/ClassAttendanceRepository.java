package com.flexydemy.content.repository;

import com.flexydemy.content.model.ClassAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
@Repository
public interface ClassAttendanceRepository extends JpaRepository<ClassAttendance, String> {
    List<ClassAttendance> findByStudentId(String studentId);

    boolean existsByStudentIdAndAttendanceTimeAfter(String studentId, Instant time);
}
