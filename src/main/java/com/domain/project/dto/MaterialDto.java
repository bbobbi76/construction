package com.domain.project.dto;

import lombok.Data;

/**
 * [자재 DTO]
 * 공사일지 작성 시 투입된 '자재(Material)'의 현황 정보를 담는 보조 객체입니다.
 * (예: 시멘트 10포, 레미콘 50루베 등)
 */
@Data // Lombok: Getter, Setter, toString, equals 등을 자동으로 생성합니다.
public class MaterialDto {

    /** 자재명 (예: 시멘트, 철근 D10, 벽돌) */
    private String name;

    /** 수량 및 단위 (예: 100kg, 10Box) */
    // 숫자와 단위가 섞일 수 있으므로 String 타입으로 처리합니다.
    private String quantity;
}