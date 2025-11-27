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
import java.util.stream.Collectors;

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
     * - 로그인한 ID(username)를 받아서 저장
     * - AI 분석 데이터(잠재위험 등)도 함께 저장
     */
    @Transactional
    public SafetyLogDto createLog(SafetyLogDto dto, String username) {
        SafetyLog entity = dtoToEntity(dto);

        // ★ [로그인 기능] 작성자 강제 주입
        entity.setAuthor(username);

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
     * 3. "내" 안전일지 목록 조회 (GET)
     * - 로그인한 사용자(author)의 글만 가져옴
     */
    public List<SafetyLogDto> findAllMyLogs(String username) {
        List<SafetyLog> entities = safetyLogRepository.findByAuthorOrderByLogDateDesc(username);
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * 4. 안전일지 수정 (PUT)
     */
    @Transactional
    public SafetyLogDto updateLog(Long id, SafetyLogDto dto) {
        SafetyLog entity = safetyLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id));

        updateEntityFromDto(entity, dto); // 데이터 업데이트

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

    private void updateEntityFromDto(SafetyLog entity, SafetyLogDto dto) {
        // 1. 기본 정보 복사
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

        // (작성자 author는 수정 시 건드리지 않음)

        // 2. ★ [AI 분석 필드 복구] 이 부분이 누락되었었습니다.
        entity.setPotentialRiskFactors(dto.getPotentialRiskFactors()); // 잠재위험
        entity.setCountermeasures(dto.getCountermeasures());           // 대책
        entity.setMajorRiskFactors(dto.getMajorRiskFactors());         // 중점위험
        entity.setFollowUpPhoto(dto.getFollowUpPhoto());               // 후속조치 사진
        entity.setCorrectiveActions(dto.getCorrectiveActions());       // 지적사항

        // 3. JSON 리스트 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setSafetyChecklist(convertListToJsonString(dto.getSafetyChecklist()));
    }

    private SafetyLog dtoToEntity(SafetyLogDto dto) {
        SafetyLog entity = new SafetyLog();
        updateEntityFromDto(entity, dto);
        return entity;
    }

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

        // ★ [AI 분석 필드 반환 복구]
        dto.setPotentialRiskFactors(entity.getPotentialRiskFactors());
        dto.setCountermeasures(entity.getCountermeasures());
        dto.setMajorRiskFactors(entity.getMajorRiskFactors());
        dto.setFollowUpPhoto(entity.getFollowUpPhoto());
        dto.setCorrectiveActions(entity.getCorrectiveActions());

        // JSON 변환
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setSafetyChecklist(convertJsonStringToSafetyCheckList(entity.getSafetyChecklist()));

        return dto;
    }

    // --- JSON 변환 유틸리티 ---
    private String convertListToJsonString(Object list) {
        if (list == null) return null;
        try { return objectMapper.writeValueAsString(list); } catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
    private List<String> convertJsonStringToListString(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); } catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
    private List<EquipmentDto> convertJsonStringToEquipmentList(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<EquipmentDto>>() {}); } catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
    private List<SafetyCheckItemDto> convertJsonStringToSafetyCheckList(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<SafetyCheckItemDto>>() {}); } catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}