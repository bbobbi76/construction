package com.domain.project.repository;

import com.domain.project.entity.ConstructionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // 이 인터페이스가 Spring의 Repository(DB 저장소)임을 선언
public interface ConstructionLogRepository extends JpaRepository<ConstructionLog, Long> {
    /**
     * JpaRepository<ConstructionLog, Long> 를 상속(extends)받는 것만으로
     * 기본적인 DB 조작 메서드(save, findById, findAll, delete 등)가
     * "자동으로" 생성됩니다.

     * - ConstructionLog: 이 Repository는 ConstructionLog Entity를 다룹니다.
     * - Long: 그 Entity의 @Id 필드(id) 타입이 Long입니다.
     */
}