package com.domain.project.dto;

/**
 * '안전 체크리스트' 보조 DTO (public 파일로 분리됨)
 */
public class SafetyCheckItemDto {
    private String item;
    private String status;

    // --- Getter/Setter ---
    public String getItem() {
        return item;
    }
    public void setItem(String item) {
        this.item = item;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}