package com.domain.project.dto;

import lombok.Data;

/**
 * '안전 체크리스트' 보조 DTO (Lombok 적용)
 */
@Data
public class SafetyCheckItemDto {
    private String item;
    private String status;
}