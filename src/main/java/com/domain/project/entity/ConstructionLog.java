package com.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ConstructionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 공통 항목 ---
    private String company;
    private String logDate;
    private String weather;
    private String location;

    @Lob private String workDetails; // (사람이 직접 쓰는 상세 작업 내용)
    private String workType;
    private int workersCount;

    @Lob private String workerNames; // JSON
    @Lob private String remarks;

    private String manager;

    @Lob private String equipment;   // JSON
    @Lob private String photos;      // 현장 사진 (AI 분석 대상)
    @Lob private String signature;
    @Lob private String attachments; // JSON

    // [로그인 기능] 작성자 ID
    private String author;

    // --- 공사일지 고유 항목 ---
    @Lob private String materials;   // 자재 JSON

    // ★ [AI 분석 필드] 사진 보고 무슨 작업인지 분석한 내용
    @Lob private String aiWorkDescription;
}