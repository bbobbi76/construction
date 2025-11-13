package com.domain.project.dto;

import lombok.Data;
import java.util.List;

/**
 * 안전일지 (SafetyLog) 메인 DTO 클래스 (Lombok 적용)
 */
@Data
public class SafetyLogDto {

    // --- 공통 항목 ---
    private Long id; // DB에서 생성된 고유 ID
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
    private String author; //작성자

    // --- 안전일지 고유 항목 ---
    private String riskFactors;
    private String correctiveActions;
    private List<SafetyCheckItemDto> safetyChecklist;

    // Getter/Setter 모두 @Data가 자동으로 생성
}