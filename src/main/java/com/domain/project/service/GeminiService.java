package com.domain.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * [Gemini AI 서비스]
 * Google의 Gemini API를 호출하여 이미지를 분석하고 텍스트 결과를 받아오는 서비스입니다.
 * RestTemplate을 사용하여 HTTP 요청을 직접 구성하고 전송합니다.
 */
@Service
public class GeminiService {

    // application.properties에서 설정한 API 키를 주입받습니다.
    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * [이미지 분석 요청]
     * 사용자가 업로드한 이미지 파일과 프롬프트(명령어)를 받아 Gemini에게 전송합니다.
     *
     * @param file   업로드된 이미지 파일 (MultipartFile)
     * @param prompt AI에게 지시할 내용 (예: "위험요인을 찾아줘")
     * @return AI가 분석한 텍스트 응답
     */
    public String analyzeImage(MultipartFile file, String prompt) {
        try {
            // 1. 사용할 모델 설정 (gemini-2.5-pro)
            String model = "gemini-2.5-pro";

            // 2. API 키 안전장치 (혹시 모를 공백이나 따옴표 제거)
            String cleanKey = apiKey.trim().replace("\"", "");

            // 3. 요청 URL 조립
            String requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + cleanKey;

            // 디버깅용 로그 출력
            System.out.println("DEBUG: AI 요청 시작 -> " + model);

            // =================================================================================
            //                                데이터 준비 (JSON 구조 만들기)
            // =================================================================================

            // 4. 이미지 인코딩 (Base64)
            // 이미지를 텍스트(JSON)로 전송하기 위해 Base64 문자열로 변환합니다.
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = file.getContentType();

            // 5. 요청 본문(Body) 구성
            // 구조: contents -> parts -> [ {text}, {inline_data(image)} ]

            // (1) 이미지 데이터 파트
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("inline_data", inlineData);

            // (2) 텍스트 프롬프트 파트
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            // (3) 콘텐츠 조합 (텍스트 + 이미지)
            Map<String, Object> content = new HashMap<>();
            content.put("parts", Arrays.asList(textPart, imagePart));

            // (4) 최종 요청 바디
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            // 6. 헤더 설정 (JSON 형식)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 7. HTTP 엔티티 생성 (헤더 + 바디)
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            // =================================================================================
            //                                API 요청 및 응답 처리
            // =================================================================================

            // 8. POST 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, entity, Map.class);

            // 9. 응답 파싱 (JSON 구조에서 text 추출)
            // 구조: candidates -> [0] -> content -> parts -> [0] -> text
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (!candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");

                    if (!parts.isEmpty()) {
                        // 최종 분석 결과 텍스트 반환
                        return (String) parts.get(0).get("text");
                    }
                }
            }

            return "AI 분석 결과 없음";

        } catch (HttpClientErrorException e) {
            // API 호출 중 4xx, 5xx 에러 발생 시 처리
            System.err.println("Gemini 통신 에러: " + e.getResponseBodyAsString());
            return "AI 요청 실패: " + e.getStatusText();

        } catch (Exception e) {
            // 기타 내부 오류 처리
            e.printStackTrace();
            return "서버 내부 오류: " + e.getMessage();
        }
    }
}