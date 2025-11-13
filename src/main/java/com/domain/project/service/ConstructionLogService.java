package com.domain.project.service;

import com.domain.project.entity.ConstructionLog;
import com.domain.project.dto.ConstructionLogDto;
import com.domain.project.dto.EquipmentDto;
import com.domain.project.dto.MaterialDto;
import com.domain.project.repository.ConstructionLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConstructionLogService {

    private final ConstructionLogRepository constructionLogRepository;
    private final ObjectMapper objectMapper;

    public ConstructionLogService(ConstructionLogRepository constructionLogRepository, ObjectMapper objectMapper) {
        this.constructionLogRepository = constructionLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 1. 공사일지 저장 (POST)
     */
    @Transactional
    public ConstructionLogDto createLog(ConstructionLogDto dto) {
        ConstructionLog entity = dtoToEntity(dto);
        ConstructionLog savedEntity = constructionLogRepository.save(entity);
        return entityToDto(savedEntity);
    }

    /**
     * 2. 공사일지 단건 조회 (GET)
     */
    public ConstructionLogDto getLogById(Long id) {
        ConstructionLog entity = constructionLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공사일지가 없습니다. id=" + id));
        return entityToDto(entity);
    }

    /**
     * 3. 공사일지 "전체" 조회 (GET)
     */
    @Transactional(readOnly = true)
    public List<ConstructionLogDto> findAllLogs() {
        List<ConstructionLog> entities = constructionLogRepository.findAll();
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * 4. 공사일지 수정 (PUT) - [리팩토링]
     */
    @Transactional
    public ConstructionLogDto updateLog(Long id, ConstructionLogDto dto) {
        // 1. ID로 기존 Entity 조회
        ConstructionLog entity = constructionLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공사일지가 없습니다. id=" + id));

        // 2. [리팩토링] 헬퍼 메서드를 호출하여 DTO의 내용으로 Entity 업데이트
        updateEntityFromDto(entity, dto);

        // 3. DB에 저장 (JPA 변경 감지)
        ConstructionLog updatedEntity = constructionLogRepository.save(entity);

        // 4. DTO로 변환하여 반환
        return entityToDto(updatedEntity);
    }

    /**
     * 5. 공사일지 삭제 (DELETE)
     */
    @Transactional
    public void deleteLog(Long id) {
        if (!constructionLogRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 공사일지가 없습니다. id=" + id);
        }
        constructionLogRepository.deleteById(id);
    }


    // --- (핵심) DTO <-> Entity 변환 헬퍼 메서드 ---

    /**
     * [신규] DTO의 데이터를 Entity 객체로 복사하는 헬퍼 메서드 (중복 제거용)
     * @param entity (신규 또는 기존 Entity)
     * @param dto (데이터를 담고 있는 DTO)
     */
    private void updateEntityFromDto(ConstructionLog entity, ConstructionLogDto dto) {
        // 1. 단순 필드 복사
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

        // 2. List -> JSON String 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setMaterials(convertListToJsonString(dto.getMaterials()));
    }

    /**
     * DTO -> Entity 변환 (DB 저장용) - [리팩토링]
     */
    private ConstructionLog dtoToEntity(ConstructionLogDto dto) {
        ConstructionLog entity = new ConstructionLog();
        // [리팩토링] 헬퍼 메서드 호출
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * Entity -> DTO 변환 (클라이언트 반환용)
     */
    private ConstructionLogDto entityToDto(ConstructionLog entity) {
        ConstructionLogDto dto = new ConstructionLogDto();

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

        // JSON String -> List 변환
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setMaterials(convertJsonStringToMaterialList(entity.getMaterials()));

        return dto;
    }

    // --- 4. JSON 변환 헬퍼 메서드 (변경 없음) ---

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

    private List<MaterialDto> convertJsonStringToMaterialList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<MaterialDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<MaterialDto>)", e);
        }
    }
}