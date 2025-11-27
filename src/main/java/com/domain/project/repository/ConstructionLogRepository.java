package com.domain.project.repository;

import com.domain.project.entity.ConstructionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // ★ List import가 꼭 필요합니다!

@Repository
public interface ConstructionLogRepository extends JpaRepository<ConstructionLog, Long> {

    /**
     * [추가된 메서드]
     * findByAuthor: 작성자(author)가 일치하는 데이터만 찾습니다.
     * OrderByLogDateDesc: 그 중에서 날짜(logDate)를 기준으로 내림차순(최신순) 정렬합니다.
     * * 즉, "내 아이디로 쓴 글만 최신순으로 가져와라"라는 뜻입니다.
     */
    List<ConstructionLog> findByAuthorOrderByLogDateDesc(String author);

}