package com.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // 아이디 중복 방지
    private String username; // 로그인 아이디

    private String password; // 암호화된 비밀번호

    private String name;     // 사용자 이름 (예: 김안전)

    private String role;     // 권한 (ROLE_USER, ROLE_ADMIN 등)
}