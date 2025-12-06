package com.domain.project.controller;

import com.domain.project.dto.ConstructionLogDto;
import com.domain.project.service.ConstructionLogService;
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
 * [공사일지 컨트롤러]
 * 공사일지 작성, 조회, 수정, 삭제(CRUD) 및
 * AI 사진 분석, PDF 다운로드 기능을 제공하는 REST API 엔드포인트입니다.
 */
@RestController
@RequestMapping("/api/construction-log")
public class ConstructionLogController {

    private final ConstructionLogService constructionLogService;
    private final GeminiService geminiService;
    private final PdfService pdfService;

    // 생성자 주입
    public ConstructionLogController(ConstructionLogService constructionLogService,
                                     GeminiService geminiService,
                                     PdfService pdfService) {
        this.constructionLogService = constructionLogService;
        this.geminiService = geminiService;
        this.pdfService = pdfService;
    }

    // =================================================================================
    //                                  CRUD 기능
    // =================================================================================

    /**
     * 1. 공사일지 작성
     * [POST] /api/construction-log
     * @param dto       클라이언트가 보낸 일지 데이터 (JSON)
     * @param principal 로그인한 사용자 정보 (Spring Security)
     * @return 저장된 일지 데이터 + 201 Created 상태코드
     */
    @PostMapping
    public ResponseEntity<ConstructionLogDto> createLog(@RequestBody ConstructionLogDto dto, Principal principal) {
        // 로그인한 사용자 아이디(username)를 가져와서 작성자로 설정
        String username = principal.getName();
        ConstructionLogDto createdDto = constructionLogService.createLog(dto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    /**
     * 2. 내 공사일지 전체 목록 조회
     * [GET] /api/construction-log
     * @return 내가 작성한 일지 리스트 (최신순 정렬)
     */
    @GetMapping
    public ResponseEntity<List<ConstructionLogDto>> getAllLogs(Principal principal) {
        String username = principal.getName();
        List<ConstructionLogDto> dtos = constructionLogService.findAllMyLogs(username);
        return ResponseEntity.ok(dtos);
    }

    /**
     * 3. 가장 최근 공사일지 1개 조회 (전일 내용 불러오기용)
     * [GET] /api/construction-log/last
     * @return 가장 최근 일지 데이터 (없으면 204 No Content)
     */
    @GetMapping("/last")
    public ResponseEntity<ConstructionLogDto> getLastLog(Principal principal) {
        try {
            String username = principal.getName();
            ConstructionLogDto dto = constructionLogService.getLastLog(username);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            // 이전에 쓴 글이 아예 없을 경우 204(No Content) 반환 -> 프론트엔드에서 처리
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 4. 특정 공사일지 상세 조회
     * [GET] /api/construction-log/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConstructionLogDto> getLog(@PathVariable Long id) {
        ConstructionLogDto dto = constructionLogService.getLogById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 5. 공사일지 수정
     * [PUT] /api/construction-log/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConstructionLogDto> updateLog(@PathVariable Long id, @RequestBody ConstructionLogDto dto) {
        ConstructionLogDto updatedDto = constructionLogService.updateLog(id, dto);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * 6. 공사일지 삭제
     * [DELETE] /api/construction-log/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        constructionLogService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    // =================================================================================
    //                                  특수 기능 (AI, PDF)
    // =================================================================================

    /**
     * 7. AI 현장 사진 분석
     * [POST] /api/construction-log/analyze-photo
     * Gemini AI를 호출하여 사진 속 공사 내용, 자재, 특이사항을 텍스트로 추출합니다.
     */
    @PostMapping("/analyze-photo")
    public ResponseEntity<Map<String, String>> analyzePhoto(@RequestParam("file") MultipartFile file) {

        // AI에게 지시할 프롬프트 설정 (인사말 제거, 개조식 강요 등 엄격한 규칙 적용)
        String prompt =
                "이 공사 현장 사진을 분석하여 작업 일지를 작성하세요.\n" +
                        "다음 **5가지 출력 규칙을 엄격히 준수**하세요.\n\n" +
                        "1. [절대 금지]: '분석 결과입니다', '사진에는~' 같은 서론/결론/인사말 절대 포함 금지.\n" +
                        "2. [줄바꿈]: 각 항목 사이에는 **반드시 빈 줄(엔터)을 하나씩 넣어** 가독성을 확보하세요.\n" +
                        "3. [말투]: '~작업 중', '~설치됨', '~식별됨' 등 **명사형으로 간결하게** 끝내세요.\n" +
                        "4. [내용]: 추측성 발언은 배제하고, 눈에 보이는 사실만 나열하세요.\n" +
                        "5. [형식]: 아래 형식을 그대로 따르세요.\n" +
                        "1. [주요 공종] 핵심 작업 내용 (예: 3층 슬라브 철근 배근 작업)\n\n" +
                        "2. [투입 자재/장비] 식별되는 자재와 장비 나열\n\n" +
                        "3. [작업 상황] 작업자들의 행동 및 현장 특이사항 요약";

        // 서비스 호출
        String analysisResult = geminiService.analyzeImage(file, prompt);

        // JSON 형태로 반환 {"description": "분석 결과..."}
        return ResponseEntity.ok(Collections.singletonMap("description", analysisResult));
    }

    /**
     * 8. PDF 다운로드
     * [GET] /api/construction-log/{id}/pdf
     * iText 라이브러리를 사용하여 일지를 PDF 파일로 변환해 반환합니다.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        // 데이터 조회 및 PDF 생성
        ConstructionLogDto dto = constructionLogService.getLogById(id);
        byte[] pdfBytes = pdfService.generateConstructionLogPdf(dto);

        // 브라우저가 파일로 인식하고 다운로드하도록 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "log_" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}