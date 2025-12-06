package com.domain.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * [날씨 서비스]
 * OpenWeatherMap 외부 API를 호출하여,
 * 사용자의 현재 위치(위도, 경도)에 따른 날씨와 기온 정보를 가져오는 서비스입니다.
 */
@Service
public class WeatherService {

    // application.properties에서 OpenWeatherMap API 키를 주입받습니다.
    @Value("${weather.api.key}")
    private String apiKey;

    // HTTP 요청을 보내기 위한 스프링 템플릿
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * [날씨 정보 조회]
     * 위도(lat)와 경도(lon)를 받아 현재 날씨 상태 문자열을 반환합니다.
     *
     * @param lat 위도
     * @param lon 경도
     * @return "맑음, 24.5°C" 형태의 문자열 (실패 시 "날씨 정보 없음")
     */
    public String getWeather(double lat, double lon) {
        try {
            // 1. API 요청 URL 생성
            // - units=metric: 섭씨 온도(°C) 사용
            // - lang=kr: 날씨 설명을 한글로 받음
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                    "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";

            // 2. 외부 API 호출 (GET 요청)
            // 응답을 유연하게 처리하기 위해 Map 클래스로 받습니다.
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                // 3. 날씨 설명 파싱 ("맑음", "구름 많음" 등)
                // JSON 구조: { "weather": [ { "description": "맑음", ... } ], ... }
                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
                String description = (String) weatherList.get(0).get("description");

                // 4. 기온 파싱 (섭씨)
                // JSON 구조: { "main": { "temp": 24.5, ... }, ... }
                Map<String, Object> mainMap = (Map<String, Object>) response.get("main");

                // API가 정수(24) 또는 실수(24.5)를 보낼 수 있으므로 Number로 안전하게 받음
                Number tempNumber = (Number) mainMap.get("temp");
                double temp = tempNumber.doubleValue();

                // 5. 결과 조합하여 반환
                return String.format("%s, %.1f°C", description, temp);
            }

        } catch (Exception e) {
            // 통신 오류나 파싱 오류 발생 시 로그를 남기고 기본값 반환
            e.printStackTrace();
            return "날씨 정보 없음";
        }

        return "날씨 정보 없음";
    }
}