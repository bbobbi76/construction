package com.domain.project.repository;

import com.domain.project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 로그인 아이디로 회원 정보 조회
    Optional<Member> findByUsername(String username);
}