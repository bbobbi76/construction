package com.domain.project.service;

// 1. 필요한 import 문들 (DTO, Entity, Repository 및 JSON 변환 도구)
import com.domain.project.entity.SafetyLog;
import com.domain.project.dto.EquipmentDto;
import com.domain.project.dto.SafetyCheckItemDto;
import com.domain.project.dto.SafetyLogDto;
import com.domain.project.repository.SafetyLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SafetyLogService {

    private final SafetyLogRepository safetyLogRepository;
    private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper

    // 2. 생성자 주입 (Repository와 ObjectMapper)
    public SafetyLogService(SafetyLogRepository safetyLogRepository, ObjectMapper objectMapper) {
        this.safetyLogRepository = safetyLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 1. 안전일지 저장 (POST)
     */
    @Transactional
    public SafetyLogDto createLog(SafetyLogDto dto) {
        SafetyLog entity = dtoToEntity(dto);
        SafetyLog savedEntity = safetyLogRepository.save(entity);
        return entityToDto(savedEntity);
    }

    /**
     * 2. 안전일지 단건 조회 (GET)
     */
    public SafetyLogDto getLogById(Long id) {
        SafetyLog entity = safetyLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id));
        return entityToDto(entity);
    }

    // --- (핵심) DTO <-> Entity 변환 헬퍼 메서드 ---

    /**
     * DTO -> Entity 변환 (DB 저장용)
     *  List 객체들을 JSON 문자열로 변환하여 Entity의 String 필드에 저장합니다.
     */
    private SafetyLog dtoToEntity(SafetyLogDto dto) {
        SafetyLog entity = new SafetyLog();

        // 1. 단순 필드 복사 (공통 항목)
        entity.setCompany(dto.getCompany());
        entity.setLogDate(dto.getLogDate());
        entity.setWeather(dto.getWeather());
        entity.setLocation(dto.getLocation());
        entity.setWorkDetails(dto.getWorkDetails());
        entity.setWorkType(dto.getWorkType());
        entity.setWorkersCount(dto.getWorkersCount());
        entity.setRemarks(dto.getRemarks());
        entity.setManager(dto.getManager());
        entity.setSignature(dto.getSignature());

        // 2. 단순 필드 복사 (안전일지 고유 항목)
        entity.setRiskFactors(dto.getRiskFactors());
        entity.setCorrectiveActions(dto.getCorrectiveActions());

        // 3.  List -> JSON String 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setSafetyChecklist(convertListToJsonString(dto.getSafetyChecklist())); // 안전 체크리스트 변환

        return entity;
    }

    /**
     * Entity -> DTO 변환 (클라이언트 반환용)
     *  Entity의 JSON 문자열 필드를 List 객체로 변환하여 DTO에 저장합니다.
     */
    private SafetyLogDto entityToDto(SafetyLog entity) {
        SafetyLogDto dto = new SafetyLogDto();

        // 1. ID 값 세팅
        dto.setId(entity.getId());

        // 2. 단순 필드 복사 (공통 항목)
        dto.setCompany(entity.getCompany());
        dto.setLogDate(entity.getLogDate());
        dto.setWeather(entity.getWeather());
        dto.setLocation(entity.getLocation());
        dto.setWorkDetails(entity.getWorkDetails());
        dto.setWorkType(entity.getWorkType());
        dto.setWorkersCount(entity.getWorkersCount());
        dto.setRemarks(entity.getRemarks());
        dto.setManager(entity.getManager());
        dto.setSignature(entity.getSignature());

        // 3. 단순 필드 복사 (안전일지 고유 항목)
        dto.setRiskFactors(entity.getRiskFactors());
        dto.setCorrectiveActions(entity.getCorrectiveActions());

        // 4. JSON String -> List 변환
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setSafetyChecklist(convertJsonStringToSafetyCheckList(entity.getSafetyChecklist())); // 안전 체크리스트 변환

        return dto;
    }

    // --- 5. JSON 변환 헬퍼 메서드 ---

    /**
     * (공통) List 객체를 JSON 문자열로 변환
     */
    private String convertListToJsonString(Object list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (List -> String)", e);
        }
    }

    /**
     * (개별) JSON 문자열을 List<String>으로 변환
     */
    private List<String> convertJsonStringToListString(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<String>)", e);
        }
    }

    /**
     * (개별) JSON 문자열을 List<EquipmentDto>로 변환
     */
    private List<EquipmentDto> convertJsonStringToEquipmentList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<EquipmentDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<EquipmentDto>)", e);
        }
    }

    /**
     * (개별) JSON 문자열을 List<SafetyCheckItemDto>로 변환
     */
    private List<SafetyCheckItemDto> convertJsonStringToSafetyCheckList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<SafetyCheckItemDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<SafetyCheckItemDto>)", e);
        }
    }
}