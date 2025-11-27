package com.domain.project.repository;

import com.domain.project.entity.SafetyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SafetyLogRepository extends JpaRepository<SafetyLog, Long> {
    // 작성자(author)로 찾고, 날짜 내림차순(최신순) 정렬
    List<SafetyLog> findByAuthorOrderByLogDateDesc(String author);
}