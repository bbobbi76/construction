package com.domain.project.dto;

import lombok.Data;
import java.util.List;

@Data
public class ConstructionLogDto {
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

    // [로그인]
    private String author;

    // [공사일지 고유]
    private List<MaterialDto> materials;

    // ★ [AI 분석 필드]
    private String aiWorkDescription;
}