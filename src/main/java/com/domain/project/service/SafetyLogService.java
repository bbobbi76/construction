package com.domain.project.service;

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
    private final ObjectMapper objectMapper;

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

    /**
     * 3. 안전일지 "전체" 조회 (GET)
     */
    @Transactional(readOnly = true)
    public List<SafetyLogDto> findAllLogs() {
        List<SafetyLog> entities = safetyLogRepository.findAll();
        return entities.stream()
                .map(this::entityToDto)
                .toList();
    }

    /**
     * 4. 안전일지 수정 (PUT) - [리팩토링]
     */
    @Transactional
    public SafetyLogDto updateLog(Long id, SafetyLogDto dto) {
        SafetyLog entity = safetyLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id));

        // [리팩토링] 헬퍼 메서드를 호출하여 DTO의 내용으로 Entity 업데이트
        updateEntityFromDto(entity, dto);

        SafetyLog updatedEntity = safetyLogRepository.save(entity);
        return entityToDto(updatedEntity);
    }

    /**
     * 5. 안전일지 삭제 (DELETE)
     */
    @Transactional
    public void deleteLog(Long id) {
        if (!safetyLogRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id);
        }
        safetyLogRepository.deleteById(id);
    }


    // --- (핵심) DTO <-> Entity 변환 헬퍼 메서드 ---

    /**
     * [신규] DTO의 데이터를 Entity 객체로 복사하는 헬퍼 메서드 (중복 제거용)
     * @param entity (신규 또는 기존 Entity)
     * @param dto (데이터를 담고 있는 DTO)
     */
    private void updateEntityFromDto(SafetyLog entity, SafetyLogDto dto) {
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
        entity.setAuthor(dto.getAuthor());

        // 2. 단순 필드 복사 (안전일지 고유 항목)
        entity.setRiskFactors(dto.getRiskFactors());
        entity.setCorrectiveActions(dto.getCorrectiveActions());

        // 3.  List -> JSON String 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setSafetyChecklist(convertListToJsonString(dto.getSafetyChecklist()));
    }


    /**
     * DTO -> Entity 변환 (DB 저장용) - [리팩토링]
     */
    private SafetyLog dtoToEntity(SafetyLogDto dto) {
        SafetyLog entity = new SafetyLog();
        // [리팩토링] 헬퍼 메서드 호출
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * Entity -> DTO 변환 (클라이언트 반환용)
     */
    private SafetyLogDto entityToDto(SafetyLog entity) {
        SafetyLogDto dto = new SafetyLogDto();

        dto.setId(entity.getId());
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
        dto.setAuthor(entity.getAuthor());
        dto.setRiskFactors(entity.getRiskFactors());
        dto.setCorrectiveActions(entity.getCorrectiveActions());

        // 4. JSON String -> List 변환
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setSafetyChecklist(convertJsonStringToSafetyCheckList(entity.getSafetyChecklist()));

        return dto;
    }

    // --- 5. JSON 변환 헬퍼 메서드 (변경 없음) ---

    private String convertListToJsonString(Object list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (List -> String)", e);
        }
    }

    private List<String> convertJsonStringToListString(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<String>)", e);
        }
    }

    private List<EquipmentDto> convertJsonStringToEquipmentList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<EquipmentDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<EquipmentDto>)", e);
        }
    }

    private List<SafetyCheckItemDto> convertJsonStringToSafetyCheckList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<SafetyCheckItemDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<SafetyCheckItemDto>)", e);
        }
    }
}