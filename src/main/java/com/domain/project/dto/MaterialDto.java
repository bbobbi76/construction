package com.domain.project.dto;

import lombok.Data;

/**
 * '자재' 보조 DTO (Lombok 적용)
 */
@Data
public class MaterialDto {
    private String name;
    private String quantity;
}