package com.domain.project.controller;

import com.domain.project.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * [파일 컨트롤러]
 * 파일 업로드 및 관리와 관련된 HTTP 요청을 처리하는 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    // 생성자 주입 (DI)
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 1. 파일 업로드 API
     * [POST] /api/files/upload
     * 클라이언트가 전송한 파일을 서버 스토리지에 저장하고, 웹에서 접근 가능한 경로를 반환합니다.
     *
     * @param file 클라이언트 form-data의 'file' 필드로 전송된 바이너리 파일
     * @return JSON {"filePath": "/uploads/uuid_파일명.jpg"}
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {

        // 1. 서비스 호출: 실제 파일 저장을 수행하고, 저장된 웹 경로(String)를 받아옵니다.
        String filePath = fileService.storeFile(file);

        // 2. 응답 데이터 생성: 프론트엔드에서 사용하기 쉽도록 JSON Map 형태로 포장합니다.
        Map<String, String> response = new HashMap<>();
        response.put("filePath", filePath);

        // 3. 응답 반환: HTTP 200 OK 상태와 함께 경로 데이터를 보냅니다.
        return ResponseEntity.ok(response);
    }
}