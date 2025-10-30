package com.domain.project.dto;

/**
 * '자재' 보조 DTO (public 파일로 분리됨)
 */
public class MaterialDto {
    private String name;
    private String quantity;

    // --- Getter/Setter ---
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getQuantity() {
        return quantity;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}