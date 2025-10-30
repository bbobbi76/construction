package com.domain.project.dto;

/**
 * '장비' 보조 DTO (public 파일로 분리됨)
 * 외부 패키지(Service 등)에서 이 클래스를 사용할 수 있도록 'public'으로 선언합니다.
 */
public class EquipmentDto {
    private String name;
    private int count;

    // --- Getter/Setter ---
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}