package com.domain.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter // Lombok: 모든 필드의 Getter 자동 생성
@Setter // Lombok: 모든 필드의 Setter 자동 생성
public class ConstructionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;
    private String logDate;
    private String weather;
    private String location;

    @Lob
    private String workDetails;

    private String workType;
    private int workersCount;

    @Lob
    private String workerNames; // JSON 문자열

    @Lob
    private String remarks;

    private String manager;

    @Lob
    private String equipment; // JSON 문자열

    @Lob
    private String photos; // JSON 문자열

    @Lob
    private String signature;

    @Lob
    private String attachments; // JSON 문자열

    private String author; //작성자

    // --- 공사일지 고유 항목 ---
    @Lob
    private String materials; // JSON 문자열

    // 모든 Getter/Setter가 Lombok에 의해 자동 생성됨
}