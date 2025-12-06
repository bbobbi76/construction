package com.domain.project.repository;

import com.domain.project.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * [회원 저장소]
 * 데이터베이스의 'MEMBER' 테이블에 접근하여
 * 회원 정보의 저장, 조회(로그인 인증), 수정, 삭제를 담당하는 인터페이스입니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * [회원 조회 - 로그인용]
     * 사용자가 입력한 아이디(username)와 일치하는 회원 정보를 DB에서 찾습니다.
     *
     * @param username 찾을 로그인 아이디
     * @return 회원 객체 (존재하지 않을 경우를 대비해 Optional로 감싸서 반환)
     */
    Optional<Member> findByUsername(String username);
}