package com.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * [회원 엔티티]
 * 시스템을 이용하는 사용자(현장 관리자 등)의 계정 정보를 저장하는 클래스입니다.
 * 데이터베이스의 'MEMBER' 테이블과 매핑되며, Spring Security 인증 시 사용됩니다.
 */
@Entity
@Getter @Setter
public class Member {

    // 식별자 (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =================================================================================
    //                                계정 정보
    // =================================================================================

    /**
     * 로그인 아이디
     * - @Column(unique = true): 데이터베이스 차원에서 중복 가입을 방지합니다.
     */
    @Column(unique = true)
    private String username;

    /**
     * 비밀번호
     * - 보안을 위해 평문이 아닌 암호화된 문자열(BCrypt)로 저장됩니다.
     */
    private String password;

    /**
     * 사용자 실명
     * - 예: 홍길동, 김안전 (화면에 표시되는 이름)
     */
    private String name;

    /**
     * 계정 권한
     * - Spring Security 처리를 위한 권한 식별자 (예: ROLE_USER, ROLE_ADMIN)
     */
    private String role;
}