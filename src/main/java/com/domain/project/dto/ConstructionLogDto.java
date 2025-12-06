package com.domain.project.dto;

import lombok.Data;
import java.util.List;

/**
 * [공사일지 DTO]
 * 클라이언트(프론트엔드)와 서버 간에 공사일지 데이터를 주고받을 때 사용하는 객체입니다.
 * Entity와 달리 화면에 보여주거나 입력받는 형태에 최적화되어 있습니다.
 */
@Data
public class ConstructionLogDto {

    // 식별자 (DB PK)
    private Long id;

    // =================================================================================
    //                                1. 기본 현장 정보
    // =================================================================================
    private String company;     // 회사명 (예: 솔아이티)
    private String logDate;     // 작업 일자 (YYYY-MM-DD)
    private String weather;     // 날씨 및 기온 (API 또는 직접 입력)
    private String location;    // 현장 위치 (주소)
    private String author;      // 작성자 ID (로그인한 사용자)
    private String manager;     // 현장 관리자 (담당자) 이름

    // =================================================================================
    //                                2. 작업 상세 내용
    // =================================================================================
    private String workType;          // 공종 (예: 토공사, 철근콘크리트공사)
    private String workDetails;       // 금일 주요 작업 내용 상세
    private int workersCount;         // 총 출력 인원 수
    private List<String> workerNames; // 투입된 작업자 명단 (리스트)

    // =================================================================================
    //                                3. 자재 및 장비 (Resources)
    // =================================================================================
    private List<EquipmentDto> equipment; // 투입 장비 리스트 (장비명, 대수)
    private List<MaterialDto> materials;  // 사용 자재 리스트 (자재명, 수량) - *공사일지 고유 항목

    // =================================================================================
    //                                4. 파일 및 서명
    // =================================================================================
    private List<String> photos;      // 현장 사진 파일 경로 목록
    private String signature;         // 관리자 전자 서명 이미지 경로
    private List<String> attachments; // 기타 첨부 파일 경로 목록

    // =================================================================================
    //                                5. AI 분석 및 기타
    // =================================================================================
    private String aiWorkDescription; // Gemini AI가 사진을 분석하여 생성한 작업 설명
    private String remarks;           // 특이사항 및 지시사항
}