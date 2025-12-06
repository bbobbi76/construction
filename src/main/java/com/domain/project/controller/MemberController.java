package com.domain.project.controller;

import com.domain.project.entity.Member;
import com.domain.project.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * [회원 컨트롤러]
 * 회원가입 등 사용자 계정 관리와 관련된 웹 요청을 처리하는 컨트롤러입니다.
 */
@Controller
public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자 주입 (Dependency Injection)
    public MemberController(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 1. 회원가입 처리
     * [POST] /register
     * 사용자가 입력한 가입 정보를 받아 데이터베이스에 저장합니다.
     *
     * @param username 아이디
     * @param password 비밀번호 (평문)
     * @param name     사용자 실명
     * @return 가입 성공 시 로그인 페이지로 리다이렉트
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String name) {

        // 1. 회원 엔티티 생성 및 데이터 바인딩
        Member member = new Member();
        member.setUsername(username);
        member.setName(name);

        // 2. 비밀번호 암호화 (보안 필수)
        // DB에 평문으로 저장되지 않도록 PasswordEncoder를 사용해 암호화합니다.
        String encodedPassword = passwordEncoder.encode(password);
        member.setPassword(encodedPassword);

        // 3. 기본 권한 부여
        member.setRole("ROLE_USER");

        // 4. DB 저장 (회원가입 완료)
        memberRepository.save(member);

        // 5. 페이지 이동: 가입이 완료되면 로그인 페이지로 보냅니다.
        return "redirect:/login.html";
    }
}