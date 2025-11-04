package com.domain.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    // 1. (중요) 파일이 저장될 서버의 실제 경로
    // (이 폴더는 Spring Boot 프로젝트의 루트 디렉토리에 생성됩니다)
    private final String uploadDir = "uploads/";

    public FileService() {
        // 2. FileService가 실행될 때, 'uploads' 폴더가 없으면 강제로 생성합니다.
        createUploadsDirectory();
    }

    private void createUploadsDirectory() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Info: 'uploads' directory created.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: Could not create upload directory!", e);
        }
    }

    /**
     * Controller에서 받은 파일을 서버 폴더에 저장합니다.
     * @param file (업로드된 파일)
     * @return 웹에서 접근 가능한 파일 경로 (예: /uploads/uuid_filename.jpg)
     */
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Error: Failed to store empty file.");
        }

        try {
            // 3. 파일명 중복 방지: UUID (랜덤 문자열) + 원본 파일명
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 4. 저장할 전체 경로 생성 (예: 'uploads/uuid_filename.jpg')
            Path destinationPath = Paths.get(uploadDir + uniqueFileName);

            // 5. (핵심) 파일을 실제 경로에 저장 (복사)
            Files.copy(file.getInputStream(), destinationPath);

            // 6. (중요) Controller와 JS에게 "웹" 경로를 반환합니다.
            //    (실제 저장 경로는 'uploads/' 이지만, 웹에서는 '/uploads/'로 접근)
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Error: Failed to store file.", e);
        }
    }
}