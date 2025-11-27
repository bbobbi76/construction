package com.domain.project.controller;

import com.domain.project.entity.Member;
import com.domain.project.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberController(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String name) {

        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password)); // 비밀번호 암호화 필수
        member.setName(name);
        member.setRole("ROLE_USER");

        memberRepository.save(member);

        return "redirect:/login.html"; // 가입 성공 시 로그인 페이지로 이동
    }
}