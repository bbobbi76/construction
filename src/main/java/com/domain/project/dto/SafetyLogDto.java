package com.domain.project.dto;

import lombok.Data;
import java.util.List;

@Data
public class SafetyLogDto {
    private Long id;
    private String company;
    private String logDate;
    private String weather;
    private String location;
    private String workDetails;
    private String workType;
    private int workersCount;
    private List<String> workerNames;
    private String remarks;
    private String manager;
    private List<EquipmentDto> equipment;
    private List<String> photos;
    private String signature;
    private List<String> attachments;
    private String author;

    // --- AI 분석 대응 항목 ---
    private List<SafetyCheckItemDto> safetyChecklist;

    private String potentialRiskFactors; // 1. 잠재위험
    private String countermeasures;      // 2. 대책
    private String majorRiskFactors;     // 3. 중점위험
    private String followUpPhoto;        // 4. 후속조치사진

    private String correctiveActions;    // 지적사항
}