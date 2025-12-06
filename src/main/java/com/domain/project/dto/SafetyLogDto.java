package com.domain.project.dto;

import lombok.Data;
import java.util.List;

/**
 * [안전일지 DTO]
 * 클라이언트와 서버 간에 안전일지(SafetyLog) 데이터를 주고받을 때 사용하는 객체입니다.
 * 현장 정보, 작업 내용뿐만 아니라 AI가 분석한 위험 요인과 대책 정보를 포함합니다.
 */
@Data // Lombok: Getter, Setter, toString, equals 등을 자동 생성
public class SafetyLogDto {

    // 식별자 (DB PK)
    private Long id;

    // =================================================================================
    //                                1. 기본 현장 정보
    // =================================================================================
    private String company;     // 회사명
    private String logDate;     // 점검 일자 (YYYY-MM-DD)
    private String weather;     // 날씨 및 기온
    private String location;    // 현장 위치
    private String author;      // 작성자 ID (로그인한 사용자)
    private String manager;     // 안전 관리자 또는 현장 소장

    // =================================================================================
    //                                2. 작업 현황
    // =================================================================================
    private String workType;          // 공종 (예: 비계공사, 용접작업)
    private String workDetails;       // 금일 작업 상세 내용
    private int workersCount;         // 투입 인원 수
    private List<String> workerNames; // 투입 인원 명단

    // =================================================================================
    //                                3. 투입 장비
    // =================================================================================
    private List<EquipmentDto> equipment; // 사용 장비 리스트

    // =================================================================================
    //                                4. AI 위험성 평가 및 조치
    // =================================================================================
    // [체크리스트]
    private List<SafetyCheckItemDto> safetyChecklist; // 공종별 안전 점검표 (항목 + 상태)

    // [AI 분석 결과]
    private String potentialRiskFactors; // 1. 잠재 위험 요인 (AI 추출)
    private String countermeasures;      // 2. 안전 대책 (AI 추출)
    private String majorRiskFactors;     // 3. 중점 관리 위험 요인 (사용자 강조)

    // [조치 사항]
    private String correctiveActions;    // 지적 사항 및 조치 결과 텍스트
    private String followUpPhoto;        // 4. 조치 후(After) 사진 경로

    // =================================================================================
    //                                5. 파일 및 서명
    // =================================================================================
    private List<String> photos;      // 현장 사진 (Before/위험요인) 목록
    private String signature;         // 안전 관리자 서명 이미지 경로
    private List<String> attachments; // 기타 첨부 파일 목록

    // ★ 이 부분이 없으면 오류가 발생합니다. (특이사항)
    private String remarks;
}