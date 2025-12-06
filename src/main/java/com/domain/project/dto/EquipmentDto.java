package com.domain.project.dto;

import lombok.Data;

/**
 * [장비 DTO]
 * 공사일지나 안전일지 작성 시, 투입된 '장비'의 정보를 담는 보조 객체입니다.
 * (예: 굴삭기 1대, 덤프트럭 2대 등)
 */
@Data // Lombok: Getter, Setter, toString, equals 등을 자동으로 생성해 줍니다.
public class EquipmentDto {

    /** 장비 명칭 (예: 02굴삭기, 스카이차) */
    private String name;

    /** 투입 대수 (수량) */
    private int count;
}