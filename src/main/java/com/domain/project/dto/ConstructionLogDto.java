package com.domain.project.dto;

import lombok.Data;
import java.util.List;

/**
 * 공사일지 (ConstructionLog) 메인 DTO 클래스 (Lombok 적용)
 */
@Data
public class ConstructionLogDto {
    private Long id; // DB에서 생성된 고유 ID
    // --- 공통 항목 ---
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

    // --- 공사일지 고유 항목 ---
    private List<MaterialDto> materials;

    // Getter/Setter 모두 @Data가 자동으로 생성
}