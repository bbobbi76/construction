package com.domain.project.repository;

import com.domain.project.entity.SafetyLog; //
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyLogRepository extends JpaRepository<SafetyLog, Long> {
    /**
     * JpaRepository<SafetyLog, Long>를 상속받으면
     * save, findById, findAll, delete 메서드가 자동으로 완성됩니다.
     *
     * - SafetyLog: 이 Repository는 SafetyLog Entity를 다룹니다.
     * - Long: 그 Entity의 @Id 필드(id) 타입이 Long입니다.
     */
}