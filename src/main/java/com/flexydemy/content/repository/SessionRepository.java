package com.flexydemy.content.repository;

import com.flexydemy.content.enums.SessionStatus;
import com.flexydemy.content.enums.SessionType;
import com.flexydemy.content.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByActiveFalse();

    Optional<Session> findByIdAndActiveTrue(String id);

    List<Session> findByTutor_TutorIdAndActiveTrue(String tutorId);

    List<Session> findByTutor_TutorIdAndActiveFalse(String tutorId);

    int countByTutor_TutorId(String tutorId);

    @Query("SELECT s FROM Session s " +
            "WHERE :userId IN elements(s.studentIds) " +
            "AND s.scheduledTime > :now " +
            "ORDER BY s.scheduledTime ASC")
    List<Session> findUpcomingSessionsByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

    // Upcoming sessions
    @Query("SELECT s FROM Session s WHERE :studentId MEMBER OF s.studentIds AND s.startDateTime >= :now ORDER BY s.startDateTime ASC")
    List<Session> findUpcomingSessionsForStudent(@Param("studentId") String studentId,
                                                 @Param("now") LocalDateTime now);

    // Past sessions
    @Query("SELECT s FROM Session s WHERE :studentId MEMBER OF s.studentIds AND s.startDateTime < :now ORDER BY s.startDateTime DESC")
    List<Session> findPastSessionsForStudent(@Param("studentId") String studentId,
                                             @Param("now") LocalDateTime now);

    int countByTutor_TutorIdAndStudentIdsContaining(String tutorId, String studentId);


    List<Session> findTop4ByTutor_TutorIdAndScheduledTimeAfterOrderByScheduledTimeAsc(String tutorId, LocalDateTime now);

    int countByTutor_TutorIdAndStartDateTimeBetween(String tutorId, LocalDateTime startOfMonth, LocalDateTime now);

    Page<Session> findByTutor_TutorId(String tutorId, Pageable pageable);

    Page<Session> findByTutor_TutorIdAndScheduledTimeAfter(String tutorId, LocalDateTime time, Pageable pageable);

    Page<Session> findByTutor_TutorIdAndSessionStatus(String tutorId, SessionStatus status, Pageable pageable);

    int countByTutor_TutorIdAndCourseId(String tutorId, String courseId);

    int countByTutor_TutorIdAndScheduledTimeBetween(String tutorId, LocalDateTime startOfThisMonth, LocalDateTime now);

    int countBySessionType(SessionType sessionType);
}
