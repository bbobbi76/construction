package com.domain.project.controller;

import com.domain.project.dto.SafetyLogDto;
import com.domain.project.service.SafetyLogService;
import com.domain.project.service.GeminiService;
import com.domain.project.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * [안전일지 컨트롤러]
 * 안전일지(SafetyLog)의 작성, 조회, 수정, 삭제(CRUD) 및
 * AI 사진 분석(위험요인 도출), PDF 다운로드 기능을 담당하는 REST API입니다.
 */
@RestController
@RequestMapping("/api/safety-log")
public class SafetyLogController {

    private final SafetyLogService safetyLogService;
    private final GeminiService geminiService;
    private final PdfService pdfService;

    // 생성자 주입 (Dependency Injection)
    public SafetyLogController(SafetyLogService safetyLogService, GeminiService geminiService, PdfService pdfService) {
        this.safetyLogService = safetyLogService;
        this.geminiService = geminiService;
        this.pdfService = pdfService;
    }

    // =================================================================================
    //                                  CRUD 기능
    // =================================================================================

    /**
     * 1. 안전일지 작성
     * [POST] /api/safety-log
     * 클라이언트로부터 받은 일지 데이터를 DB에 저장합니다.
     *
     * @param dto       일지 데이터 (JSON)
     * @param principal 로그인한 사용자 정보
     * @return 저장된 일지 객체 + 201 Created
     */
    @PostMapping
    public ResponseEntity<SafetyLogDto> createLog(@RequestBody SafetyLogDto dto, Principal principal) {
        String username = principal.getName();
        SafetyLogDto createdDto = safetyLogService.createLog(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    /**
     * 2. 내 안전일지 전체 조회
     * [GET] /api/safety-log
     * 로그인한 사용자가 작성한 모든 안전일지를 최신순으로 조회합니다.
     */
    @GetMapping
    public ResponseEntity<List<SafetyLogDto>> getAllLogs(Principal principal) {
        String username = principal.getName();
        List<SafetyLogDto> dtos = safetyLogService.findAllMyLogs(username);
        return ResponseEntity.ok(dtos);
    }

    /**
     * 3. 가장 최신 안전일지 1개 조회
     * [GET] /api/safety-log/last
     * 작성 화면 진입 시, 전일(가장 최근) 데이터를 자동으로 불러오기 위해 사용합니다.
     * @return 데이터가 있으면 200 OK, 없으면 204 No Content
     */
    @GetMapping("/last")
    public ResponseEntity<SafetyLogDto> getLastLog(Principal principal) {
        try {
            String username = principal.getName();
            SafetyLogDto dto = safetyLogService.getLastLog(username);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            // 이전에 작성한 글이 없는 경우 (정상적인 상황)
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 4. 특정 안전일지 상세 조회
     * [GET] /api/safety-log/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SafetyLogDto> getLog(@PathVariable Long id) {
        return ResponseEntity.ok(safetyLogService.getLogById(id));
    }

    /**
     * 5. 안전일지 수정
     * [PUT] /api/safety-log/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SafetyLogDto> updateLog(@PathVariable Long id, @RequestBody SafetyLogDto dto) {
        return ResponseEntity.ok(safetyLogService.updateLog(id, dto));
    }

    /**
     * 6. 안전일지 삭제
     * [DELETE] /api/safety-log/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        safetyLogService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    // =================================================================================
    //                                  특수 기능 (AI, PDF)
    // =================================================================================

    /**
     * 7. AI 위험성 평가 (사진 분석)
     * [POST] /api/safety-log/analyze-photo
     * Gemini AI에게 현장 사진을 보내 위험 요인과 대책을 분석받습니다.
     * 프롬프트를 통해 '인사말 제외', '개조식 작성', '특정 포맷 준수'를 강제합니다.
     */
    @PostMapping("/analyze-photo")
    public ResponseEntity<Map<String, String>> analyzePhoto(@RequestParam("file") MultipartFile file) {

        // AI 지시 사항 (Strict Prompt)
        String prompt =
                "이 건설 현장 사진을 분석하여 위험요인과 대책을 추출하세요.\n" +
                        "다음 **5가지 출력 규칙을 엄격히 준수**하세요. 어길 시 오류로 간주합니다.\n\n" +
                        "1. [절대 금지]: '안녕하십니까', '분석 결과입니다', '### 위험요인' 같은 인사말, 제목, 서론, 결론을 **일절 포함하지 마세요.** 바로 1번 항목부터 시작하세요.\n" +
                        "2. [줄바꿈]: 각 번호 항목 사이에는 **반드시 빈 줄(엔터)을 하나씩 넣어** 가독성을 확보하세요.\n" +
                        "3. [말투]: 문장은 '~함', '~임', '~상태' 등 **명사형이나 개조식으로 간결하게** 끝내세요. (존댓말 금지)\n" +
                        "4. [내용]: 위험 등급(고위험/중위험)을 포함하여 핵심만 1~2줄로 요약하세요.\n" +
                        "5. [형식]: 아래 형식을 토씨 하나 틀리지 말고 지키세요.\n" +
                        "1. [등급] 내용\n\n" +
                        "2. [등급] 내용\n\n" +
                        "..." +
                        "///" +
                        "1. 대책 내용\n\n" +
                        "2. 대책 내용";

        // Gemini Service 호출
        String analysisResult = geminiService.analyzeImage(file, prompt);

        return ResponseEntity.ok(Collections.singletonMap("description", analysisResult));
    }

    /**
     * 8. PDF 다운로드
     * [GET] /api/safety-log/{id}/pdf
     * 안전일지 내용을 PDF로 변환하여 다운로드합니다.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        // 데이터 조회 및 PDF 생성
        SafetyLogDto dto = safetyLogService.getLogById(id);
        byte[] pdfBytes = pdfService.generateSafetyLogPdf(dto);

        // 헤더 설정 (다운로드 파일명 지정)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "safety_log_" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}