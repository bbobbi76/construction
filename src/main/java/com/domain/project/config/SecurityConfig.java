package com.domain.project.config;

import com.domain.project.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호를 안전하게 암호화하는 도구
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 페이지 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, 정적 리소스(css, js, 이미지)는 누구나 접근 가능
                        .requestMatchers("/login.html", "/signup.html", "/register", "/css/**", "/js/**", "/uploads/**", "/").permitAll()
                        // 그 외(일지 작성, 목록 보기 등)는 로그인해야 접근 가능
                        .anyRequest().authenticated()
                )
                // 2. 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login.html") // 우리가 만들 커스텀 로그인 페이지
                        .loginProcessingUrl("/login") // form action="/login"으로 보낼 주소 (스프링이 알아서 처리함)
                        .defaultSuccessUrl("/index.html", true) // 로그인 성공 시 이동할 곳
                        .permitAll()
                )
                // 3. 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html")
                )
                // 4. CSRF 보안 끄기 (개발 편의성 위해, AJAX 요청 시 복잡함 방지)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // 5. 실제 로그인 로직 (DB에서 회원 찾기)
    @Bean
    public UserDetailsService userDetailsService(MemberRepository memberRepository) {
        return username -> {
            com.domain.project.entity.Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

            return User.builder()
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .roles("USER")
                    .build();
        };
    }
}