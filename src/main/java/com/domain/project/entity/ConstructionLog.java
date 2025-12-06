package com.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * [공사일지 엔티티]
 * 데이터베이스의 테이블과 1:1로 매핑되는 클래스입니다.
 * 공사 현장의 기본 정보, 작업 내용, 자원(자재/장비), 파일 경로 등을 저장합니다.
 */
@Entity
@Getter @Setter
public class ConstructionLog {

    // 식별자 (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =================================================================================
    //                                1. 기본 현장 정보
    // =================================================================================
    private String company;     // 시공사 (예: 솔아이티)
    private String logDate;     // 작업 일자 (YYYY-MM-DD)
    private String weather;     // 날씨 정보 (맑음, 24°C)
    private String location;    // 현장 위치 (주소)
    private String author;      // 작성자 계정 (로그인 ID) - Member 객체가 아닌 String으로 저장
    private String manager;     // 현장 대리인/소장 이름

    // =================================================================================
    //                                2. 작업 내용
    // =================================================================================
    private String workType;    // 공종 (예: 토공사, 철근콘크리트)
    private int workersCount;   // 총 출력 인원

    @Lob // Large Object: 긴 텍스트 저장
    private String workDetails; // 상세 작업 내용 (사용자 직접 입력)

    @Lob
    private String workerNames; // 투입 작업자 명단 (List -> JSON 문자열 변환 저장)

    // =================================================================================
    //                                3. 자재 및 장비 (JSON 저장)
    // =================================================================================
    @Lob
    private String equipment;   // 투입 장비 리스트 (EquipmentDto List -> JSON)

    @Lob
    private String materials;   // 사용 자재 리스트 (MaterialDto List -> JSON)

    // =================================================================================
    //                                4. 파일 및 서명
    // =================================================================================
    @Lob
    private String photos;      // 현장 사진 경로 리스트 (JSON)

    @Lob
    private String signature;   // 관리자 서명 이미지 경로

    @Lob
    private String attachments; // 기타 첨부파일 경로 리스트 (JSON)

    // =================================================================================
    //                                5. AI 분석 및 기타
    // =================================================================================
    @Lob
    private String aiWorkDescription; // AI가 사진을 분석하여 생성한 작업 설명

    @Lob
    private String remarks;     // 특이사항 및 지시사항
}