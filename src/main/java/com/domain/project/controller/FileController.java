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

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * [POST] /api/files/upload
     * 클라이언트(JS)에서 보낸 파일을 서버 폴더에 저장합니다.
     *
     * @param file (폼 데이터의 'file' 키로 전송된 실제 파일)
     * @return 저장된 파일의 웹 경로 (예: {"filePath": "/uploads/uuid_filename.jpg"})
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {

        // 1. FileService를 호출하여 파일을 저장하고, 저장된 경로를 반환받습니다.
        String filePath = fileService.storeFile(file);

        // 2. JS가 사용하기 쉽도록 JSON 형태로 {"filePath": "..."} 맵을 만듭니다.
        Map<String, String> response = new HashMap<>();
        response.put("filePath", filePath);

        // 3. 200 OK 상태와 함께 JSON 맵을 반환합니다.
        return ResponseEntity.ok(response);
    }
}