package com.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * [안전일지 엔티티]
 * 안전 관리 활동 내역, 위험성 평가(AI 분석), TBM 활동 등을 저장하는 클래스입니다.
 * 데이터베이스의 'SAFETY_LOG' 테이블과 1:1로 매핑됩니다.
 */
@Entity
@Getter @Setter
public class SafetyLog {

    // 식별자 (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =================================================================================
    //                                1. 기본 현장 정보
    // =================================================================================
    private String company;     // 시공사/협력사 명
    private String logDate;     // 점검 일자 (YYYY-MM-DD)
    private String weather;     // 날씨 및 기온 정보
    private String location;    // 현장 위치
    private String author;      // 작성자 계정 (로그인 ID)
    private String manager;     // 안전 관리자 또는 현장 소장 이름

    // =================================================================================
    //                                2. 작업 현황
    // =================================================================================
    private String workType;    // 공종 (예: 비계공사, 용접작업)
    private int workersCount;   // 금일 투입 인원 수

    @Lob // Large Object (긴 텍스트 저장)
    private String workDetails; // 금일 작업 내용 상세

    @Lob
    private String workerNames; // 작업자 명단 (List -> JSON 문자열 변환 저장)

    @Lob
    private String equipment;   // 투입 장비 리스트 (EquipmentDto List -> JSON)

    // =================================================================================
    //                                3. AI 위험성 평가 & 조치
    // =================================================================================
    @Lob
    private String safetyChecklist;      // 공종별 안전 점검표 (JSON)

    @Lob
    private String potentialRiskFactors; // 1. 잠재 위험 요인 (AI 분석 결과)

    @Lob
    private String countermeasures;      // 2. 안전 대책 (AI 분석 결과)

    @Lob
    private String majorRiskFactors;     // 3. 중점 관리 위험 요인 (사용자 강조 사항)

    @Lob
    private String correctiveActions;    // 지적 사항 및 조치 결과

    // =================================================================================
    //                                4. 파일 및 서명
    // =================================================================================
    @Lob
    private String photos;          // 현장 사진 (위험 요인/Before) 경로 JSON

    @Lob
    private String followUpPhoto;   // 조치 후 (After) 사진 경로

    @Lob
    private String signature;       // 관리자 서명 이미지 경로

    @Lob
    private String attachments;     // 기타 첨부 파일 경로 JSON

    @Lob
    private String remarks;         // 기타 특이사항 및 지시사항
}