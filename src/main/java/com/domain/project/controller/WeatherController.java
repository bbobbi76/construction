package com.domain.project.controller;

import com.domain.project.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * [날씨 컨트롤러]
 * 클라이언트(프론트엔드)로부터 위치 정보(위도, 경도)를 받아
 * 현재 날씨 정보를 조회하여 반환하는 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    // 생성자 주입 (Dependency Injection)
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * 1. 날씨 정보 조회 API
     * [GET] /api/weather?lat={위도}&lon={경도}
     *
     * 프론트엔드에서 사용자의 위치(Geolocation) 정보를 보내면,
     * 외부 API를 통해 현재 날씨와 기온을 가져와 반환합니다.
     *
     * @param lat 위도 (Latitude)
     * @param lon 경도 (Longitude)
     * @return JSON {"info": "맑음, 24.5°C"}
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getWeather(@RequestParam double lat, @RequestParam double lon) {

        // 1. 서비스 호출: 위도/경도 값을 이용해 날씨 문자열을 가져옵니다.
        String weatherInfo = weatherService.getWeather(lat, lon);

        // 2. 응답 생성: JSON 포맷으로 감싸서 반환합니다.
        // 예: { "info": "구름 조금, 18.2°C" }
        return ResponseEntity.ok(Collections.singletonMap("info", weatherInfo));
    }
}