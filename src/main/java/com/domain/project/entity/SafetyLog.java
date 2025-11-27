package com.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SafetyLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공통
    private String company;
    private String logDate;
    private String weather;
    private String location;
    @Lob private String workDetails;
    private String workType;
    private int workersCount;
    @Lob private String workerNames;
    @Lob private String remarks;
    private String manager;
    @Lob private String equipment;
    @Lob private String photos;
    @Lob private String signature;
    @Lob private String attachments;

    // ★ 로그인 (작성자)
    private String author;

    // ★ AI 분석
    @Lob private String safetyChecklist;
    @Lob private String potentialRiskFactors;
    @Lob private String countermeasures;
    @Lob private String majorRiskFactors;
    @Lob private String followUpPhoto;
    @Lob private String correctiveActions;
}