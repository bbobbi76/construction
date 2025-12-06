package com.domain.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * [파일 서비스]
 * 사용자가 업로드한 파일을 서버의 로컬 디스크(폴더)에 저장하고,
 * 웹 브라우저에서 접근할 수 있는 경로(URL)를 반환하는 기능을 제공합니다.
 */
@Service
public class FileService {

    // 파일이 실제로 저장될 서버 내부의 폴더 경로 (프로젝트 루트 기준 'uploads/' 폴더)
    private final String uploadDir = "uploads/";

    /**
     * 생성자: 서비스가 시작될 때 저장 폴더가 없으면 자동으로 생성합니다.
     */
    public FileService() {
        createUploadsDirectory();
    }

    /**
     * [폴더 생성 로직]
     * 'uploads' 폴더가 실제 존재하는지 확인하고, 없으면 만듭니다.
     */
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
     * [파일 저장 메서드]
     * 1. 클라이언트가 보낸 파일을 받아서
     * 2. 이름 중복을 방지하기 위해 UUID(랜덤 문자열)를 붙이고
     * 3. 서버의 'uploads' 폴더에 저장한 뒤
     * 4. 웹에서 접근 가능한 URL 경로 문자열을 반환합니다.
     *
     * @param file 업로드된 파일 객체 (MultipartFile)
     * @return 웹 접근 경로 (예: "/uploads/uuid_filename.jpg")
     */
    public String storeFile(MultipartFile file) {
        // 1. 빈 파일 체크
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Error: Failed to store empty file.");
        }

        try {
            // 2. 파일명 중복 방지 처리
            // 사용자가 올린 파일명이 같으면 덮어쓰기 될 수 있으므로, 앞에 랜덤 문자열(UUID)을 붙입니다.
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 3. 저장할 전체 경로 설정 (예: uploads/abcdef-1234_photo.jpg)
            Path destinationPath = Paths.get(uploadDir + uniqueFileName);

            // 4. 실제 저장 (파일의 데이터를 읽어서 목적지 경로에 복사합니다)
            Files.copy(file.getInputStream(), destinationPath);

            // 5. 웹 접근 경로 반환
            // 서버의 물리적 경로는 "uploads/" 이지만,
            // 웹 브라우저(HTML/JS)에서는 WebConfig 설정에 따라 "/uploads/" 경로로 접근해야 합니다.
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Error: Failed to store file.", e);
        }
    }
}