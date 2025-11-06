package com.domain.project.service;

// 1. 필요한 import 문들 (Jackson 라이브러리 추가)
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

import java.util.List; //  List import 추가
import java.util.stream.Collectors; //  Collectors import 추가

@Service
@Transactional(readOnly = true)
public class ConstructionLogService {

    private final ConstructionLogRepository constructionLogRepository;
    private final ObjectMapper objectMapper; // 2. JSON 변환을 위한 ObjectMapper 주입

    // 3. 생성자 주입 (Repository와 ObjectMapper)
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
     *  3. 공사일지 "전체" 조회 (GET)
     * (log-list.js의 '로딩 실패' 해결용)
     */
    @Transactional(readOnly = true)
    public List<ConstructionLogDto> findAllLogs() {
        // 1. DB에서 모든 Entity 조회
        List<ConstructionLog> entities = constructionLogRepository.findAll();

        // 2. Entity 리스트 -> DTO 리스트 변환 (기존 entityToDto 헬퍼 재활용)
        return entities.stream()
                .map(this::entityToDto) //
                .collect(Collectors.toList());
    }

    // --- (핵심) DTO <-> Entity 변환 헬퍼 메서드 ---

    /**
     * DTO -> Entity 변환 (DB 저장용)
     *  List 객체들을 JSON 문자열로 변환하여 Entity의 String 필드에 저장합니다.
     */
    private ConstructionLog dtoToEntity(ConstructionLogDto dto) {
        ConstructionLog entity = new ConstructionLog();

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

        // 2.  List -> JSON String 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setMaterials(convertListToJsonString(dto.getMaterials()));

        return entity;
    }

    /**
     * Entity -> DTO 변환 (클라이언트 반환용)
     *  Entity의 JSON 문자열 필드를 List 객체로 변환하여 DTO에 저장합니다.
     */
    private ConstructionLogDto entityToDto(ConstructionLog entity) {
        ConstructionLogDto dto = new ConstructionLogDto();

        // 1. ID 값 세팅 (필수!)
        dto.setId(entity.getId());

        // 2. 단순 필드 복사
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

        // 3. [수정] JSON String -> List 변환
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setMaterials(convertJsonStringToMaterialList(entity.getMaterials()));

        return dto;
    }

    // --- 4. JSON 변환 헬퍼 메서드 ---

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
     * (개별) JSON 문자열을 List<MaterialDto>로 변환
     */
    private List<MaterialDto> convertJsonStringToMaterialList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<MaterialDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 (String -> List<MaterialDto>)", e);
        }
    }
}