package com.domain.project.controller;

// 1. 필요한 import 문들
import com.domain.project.dto.ConstructionLogDto;
import com.domain.project.service.ConstructionLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 2. "이 클래스는 REST API의 요청을 받는 Controller입니다."
@RequestMapping("/api/construction-log") // 3. "이 컨트롤러의 모든 API 주소는 /api/construction-log로 시작합니다."
public class ConstructionLogController {

    private final ConstructionLogService constructionLogService; // Service를 주입받습니다.

    // 4. 생성자 주입
    public ConstructionLogController(ConstructionLogService constructionLogService) {
        this.constructionLogService = constructionLogService;
    }

    /**
     * [POST] /api/construction-log
     * 공사일지 1건 생성 (저장)
     */
    @PostMapping
    public ResponseEntity<ConstructionLogDto> createLog(@RequestBody ConstructionLogDto dto) {
        // @RequestBody: HTTP 요청의 Body(본문)에 담긴 JSON 데이터를 DTO 객체로 변환

        // 5. Service의 createLog 메서드 호출
        ConstructionLogDto createdDto = constructionLogService.createLog(dto);

        // 6. HTTP 201 Created 상태 코드와 함께,
        //    DB에 저장된 데이터(id 포함)를 클라이언트에게 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    /**
     * [GET] /api/construction-log/{id}
     * 공사일지 1건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConstructionLogDto> getLog(@PathVariable Long id) {
        // @PathVariable: URL 주소(예: /1)에 포함된 숫자 1을 id 변수에 넣어줌

        // 7. Service의 getLogById 메서드 호출
        ConstructionLogDto dto = constructionLogService.getLogById(id);

        // 8. HTTP 200 OK 상태 코드와 함께 조회된 DTO를 반환
        return ResponseEntity.ok(dto);
    }
}