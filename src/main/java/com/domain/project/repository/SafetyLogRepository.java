package com.domain.project.repository;

import com.domain.project.entity.SafetyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * [안전일지 저장소]
 * 데이터베이스의 'SAFETY_LOG' 테이블에 접근하여
 * 데이터의 저장, 조회, 수정, 삭제(CRUD)를 담당하는 인터페이스입니다.
 */
@Repository
public interface SafetyLogRepository extends JpaRepository<SafetyLog, Long> {

    /**
     * 1. [나의 일지 전체 조회]
     * 특정 작성자(author)가 작성한 모든 안전일지를 조회합니다.
     * - OrderByLogDateDesc: 날짜(logDate) 기준 내림차순(최신순)으로 정렬합니다.
     *
     * @param author 작성자 아이디
     * @return 해당 사용자의 안전일지 리스트 (최신순)
     */
    List<SafetyLog> findByAuthorOrderByLogDateDesc(String author);

    /**
     * 2. [가장 최근 일지 조회]
     * 특정 작성자(author)의 글 중에서 가장 최신(Top 1) 일지 하나만 조회합니다.
     * - 작성 화면에서 '전일 데이터 불러오기' 기능을 구현할 때 사용됩니다.
     *
     * @param author 작성자 아이디
     * @return 가장 최근 작성된 안전일지 (존재하지 않을 수 있으므로 Optional 반환)
     */
    Optional<SafetyLog> findTopByAuthorOrderByLogDateDesc(String author);
}