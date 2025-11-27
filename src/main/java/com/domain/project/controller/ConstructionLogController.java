package com.domain.project.controller;

import com.domain.project.dto.ConstructionLogDto;
import com.domain.project.service.ConstructionLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // ★ 중요: 현재 로그인한 사용자 정보를 담고 있는 객체
import java.util.List;

@RestController
@RequestMapping("/api/construction-log")
public class ConstructionLogController {

    private final ConstructionLogService constructionLogService;

    public ConstructionLogController(ConstructionLogService constructionLogService) {
        this.constructionLogService = constructionLogService;
    }

    /**
     * [POST] 공사일지 생성
     * - Principal 객체를 통해 로그인한 사용자의 아이디(username)를 가져옵니다.
     */
    @PostMapping
    public ResponseEntity<ConstructionLogDto> createLog(@RequestBody ConstructionLogDto dto, Principal principal) {
        // 1. 로그인한 사용자 아이디 꺼내기
        String username = principal.getName();

        // 2. 서비스에 DTO와 사용자 아이디를 함께 전달
        ConstructionLogDto createdDto = constructionLogService.createLog(dto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    /**
     * [GET] 공사일지 목록 조회
     * - 전체 목록이 아닌, 로그인한 사용자가 작성한 목록만 가져옵니다.
     */
    @GetMapping
    public ResponseEntity<List<ConstructionLogDto>> getAllLogs(Principal principal) {
        // 1. 로그인한 사용자 아이디 꺼내기
        String username = principal.getName();

        // 2. "내" 일지 목록만 조회하는 서비스 메서드 호출
        List<ConstructionLogDto> dtos = constructionLogService.findAllMyLogs(username);

        return ResponseEntity.ok(dtos);
    }

    /**
     * [GET] 공사일지 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConstructionLogDto> getLog(@PathVariable Long id) {
        ConstructionLogDto dto = constructionLogService.getLogById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * [PUT] 공사일지 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConstructionLogDto> updateLog(@PathVariable Long id, @RequestBody ConstructionLogDto dto) {
        ConstructionLogDto updatedDto = constructionLogService.updateLog(id, dto);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * [DELETE] 공사일지 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        constructionLogService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}