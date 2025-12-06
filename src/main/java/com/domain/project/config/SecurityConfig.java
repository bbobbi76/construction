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

    /**
     * [비밀번호 암호화 빈 등록]
     * BCrypt 해싱 함수를 사용하여 비밀번호를 안전하게 암호화합니다.
     * DB에 비밀번호를 평문으로 저장하지 않기 위해 필수적입니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [시큐리티 필터 체인 설정]
     * HTTP 요청에 대한 보안 규칙(인증, 인가, 로그인, 로그아웃 등)을 정의합니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. [URL별 권한 관리]
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입 관련 페이지 및 정적 리소스(CSS, JS, 이미지, 업로드 파일)는 누구나 접근 허용
                        .requestMatchers(
                                "/",
                                "/login.html",
                                "/signup.html",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/uploads/**",
                                // ▼ Swagger UI 접속 허용 설정 추가 ▼
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // 그 외의 모든 요청(일지 작성, 목록 조회 등)은 로그인한 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

                // 2. [로그인 설정]
                .formLogin(form -> form
                        .loginPage("/login.html")                 // 사용자 정의 로그인 페이지 경로
                        .loginProcessingUrl("/login")             // HTML 폼의 action="/login"과 일치해야 함 (스프링 시큐리티가 가로채서 처리)
                        .defaultSuccessUrl("/index.html", true)   // 로그인 성공 시 이동할 기본 페이지
                        .permitAll()                              // 로그인 페이지 자체는 누구나 접근 가능해야 함
                )

                // 3. [로그아웃 설정]
                .logout(logout -> logout
                        .logoutUrl("/logout")                     // 로그아웃 요청을 처리할 URL (POST/GET 확인 필요, 기본은 POST 권장)
                        .logoutSuccessUrl("/login.html")          // 로그아웃 성공 후 리다이렉트할 경로
                )

                // 4. [CSRF 비활성화]
                // 개발 편의성 및 REST API/AJAX 통신 시 복잡도를 줄이기 위해 비활성화 처리
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * [사용자 인증 로직]
     * DB에서 회원을 조회하여 스프링 시큐리티가 인증을 수행할 수 있도록 정보를 제공합니다.
     */
    @Bean
    public UserDetailsService userDetailsService(MemberRepository memberRepository) {
        return username -> {
            // 1. DB에서 username으로 회원 정보 조회
            com.domain.project.entity.Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

            // 2. UserDetails 객체 생성하여 반환 (비밀번호 검증은 시큐리티가 자동으로 수행)
            return User.builder()
                    .username(member.getUsername())
                    .password(member.getPassword()) // DB에 저장된 암호화된 비밀번호
                    .roles("USER")                  // 기본 권한 부여 (필요 시 member.getRole()로 동적 할당 가능)
                    .build();
        };
    }
}