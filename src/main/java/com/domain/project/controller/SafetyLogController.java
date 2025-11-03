package com.domain.project.controller;

// 1. 필요한 import 문들
import com.domain.project.dto.SafetyLogDto;
import com.domain.project.service.SafetyLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 2. REST API 컨트롤러
@RequestMapping("/api/safety-log") // 3. API 기본 주소
public class SafetyLogController {

    private final SafetyLogService safetyLogService; // Service 주입

    // 4. 생성자 주입
    public SafetyLogController(SafetyLogService safetyLogService) {
        this.safetyLogService = safetyLogService;
    }

    /**
     * [POST] /api/safety-log
     * 안전일지 1건 생성 (저장)
     */
    @PostMapping
    public ResponseEntity<SafetyLogDto> createLog(@RequestBody SafetyLogDto dto) {
        // 5. Service 호출
        SafetyLogDto createdDto = safetyLogService.createLog(dto);

        // 6. 201 Created 응답
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    /**
     * [GET] /api/safety-log/{id}
     * 안전일지 1건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<SafetyLogDto> getLog(@PathVariable Long id) {
        // 7. Service 호출
        SafetyLogDto dto = safetyLogService.getLogById(id);

        // 8. 200 OK 응답
        return ResponseEntity.ok(dto);
    }
}