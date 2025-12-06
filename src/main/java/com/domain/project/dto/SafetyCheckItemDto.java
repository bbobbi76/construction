package com.domain.project.dto;

import lombok.Data;

/**
 * [안전 점검 항목 DTO]
 * 안전일지 작성 시, 체크리스트의 '개별 항목'과 그 '결과'를 담는 객체입니다.
 * (예: "안전모 착용 상태" - "양호")
 */
@Data // Lombok: Getter, Setter, toString, equals 등을 자동으로 생성합니다.
public class SafetyCheckItemDto {

    /** 점검 내용 (예: 작업 전 전원 차단 여부, 보호구 착용 상태) */
    private String item;

    /** 점검 결과 (예: 양호, 조치필요, 해당없음) */
    private String status;
}