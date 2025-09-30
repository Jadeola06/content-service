package com.flexydemy.content.repository;

import com.flexydemy.content.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, String> {
    List<Achievement> findTop2ByUserIdOrderByAchievedAtDesc(String userId);

}
