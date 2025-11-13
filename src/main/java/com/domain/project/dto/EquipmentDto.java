package com.domain.project.dto;

import lombok.Data;

/**
 * '장비' 보조 DTO (Lombok 적용)
 * @Data: Getter, Setter, toString, equals, hashCode 등을 자동으로 생성합니다.
 */
@Data
public class EquipmentDto {
    private String name;
    private int count;
}