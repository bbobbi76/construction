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

/**
 * [공사일지 서비스]
 * 공사일지 관련 비즈니스 로직을 처리하는 계층입니다.
 * - Controller와 Repository 사이에서 데이터를 가공(DTO <-> Entity 변환)하고,
 * - 트랜잭션(Transaction)을 관리합니다.
 */
@Service
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화를 위해 읽기 전용으로 설정
public class ConstructionLogService {

    private final ConstructionLogRepository constructionLogRepository;
    private final ObjectMapper objectMapper; // JSON 변환기 (List <-> String)

    // 생성자 주입 (Dependency Injection)
    public ConstructionLogService(ConstructionLogRepository constructionLogRepository, ObjectMapper objectMapper) {
        this.constructionLogRepository = constructionLogRepository;
        this.objectMapper = objectMapper;
    }

    // =================================================================================
    //                                  CRUD 기능
    // =================================================================================

    /**
     * 1. 공사일지 생성 (저장)
     * - DTO를 Entity로 변환 후 DB에 저장합니다.
     * - 작성자(author) 정보를 주입하여 누가 썼는지 기록합니다.
     */
    @Transactional // 쓰기 작업이므로 readOnly = false (기본값)
    public ConstructionLogDto createLog(ConstructionLogDto dto, String username) {
        // 1. DTO -> Entity 변환
        ConstructionLog entity = dtoToEntity(dto);

        // 2. 작성자(로그인 사용자 ID) 설정
        entity.setAuthor(username);

        // 3. DB 저장
        ConstructionLog savedEntity = constructionLogRepository.save(entity);

        // 4. Entity -> DTO 변환 후 반환
        return entityToDto(savedEntity);
    }

    /**
     * 2. 공사일지 상세 조회
     * - ID로 일지를 찾아 반환합니다. 없으면 예외를 발생시킵니다.
     */
    public ConstructionLogDto getLogById(Long id) {
        ConstructionLog entity = constructionLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공사일지가 없습니다. id=" + id));
        return entityToDto(entity);
    }

    /**
     * 3. 내 공사일지 목록 조회
     * - 로그인한 사용자(username)가 작성한 글만 최신순으로 가져옵니다.
     */
    public List<ConstructionLogDto> findAllMyLogs(String username) {
        List<ConstructionLog> entities = constructionLogRepository.findByAuthorOrderByLogDateDesc(username);

        // Stream API를 사용하여 Entity 리스트를 DTO 리스트로 일괄 변환
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * 4. 공사일지 수정
     * - 기존 데이터를 불러와서 변경된 내용만 덮어씁니다(Dirty Checking).
     */
    @Transactional
    public ConstructionLogDto updateLog(Long id, ConstructionLogDto dto) {
        // 1. 기존 데이터 조회
        ConstructionLog entity = constructionLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공사일지가 없습니다. id=" + id));

        // 2. 데이터 업데이트 (DTO의 내용으로 Entity 필드 갱신)
        updateEntityFromDto(entity, dto);

        // 3. 저장 (JPA 변경 감지에 의해 자동 Update 쿼리 발생)
        ConstructionLog updatedEntity = constructionLogRepository.save(entity);
        return entityToDto(updatedEntity);
    }

    /**
     * 5. 공사일지 삭제
     */
    @Transactional
    public void deleteLog(Long id) {
        if (!constructionLogRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 공사일지가 없습니다. id=" + id);
        }
        constructionLogRepository.deleteById(id);
    }

    /**
     * 6. 가장 최근 일지 조회 (전일 데이터 불러오기용)
     * - 작성 화면 진입 시, 지난번 작성한 내용을 편하게 불러오기 위함입니다.
     */
    public ConstructionLogDto getLastLog(String username) {
        ConstructionLog entity = constructionLogRepository.findTopByAuthorOrderByLogDateDesc(username)
                .orElseThrow(() -> new IllegalArgumentException("작성된 이전 일지가 없습니다."));
        return entityToDto(entity);
    }

    // =================================================================================
    //                            Entity <-> DTO 변환 로직
    // =================================================================================

    /**
     * DTO의 데이터를 Entity에 업데이트 (수정 및 생성 공통 로직)
     * - List 타입의 필드(작업자, 사진, 자재 등)는 JSON String으로 변환하여 저장합니다.
     */
    private void updateEntityFromDto(ConstructionLog entity, ConstructionLogDto dto) {
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
        entity.setAiWorkDescription(dto.getAiWorkDescription()); // AI 분석 결과

        // 리스트 -> JSON 문자열 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setMaterials(convertListToJsonString(dto.getMaterials()));
    }

    /**
     * DTO -> Entity 변환
     */
    private ConstructionLog dtoToEntity(ConstructionLogDto dto) {
        ConstructionLog entity = new ConstructionLog();
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * Entity -> DTO 변환
     * - DB에 저장된 JSON 문자열을 다시 List 객체로 복원합니다.
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
        dto.setAiWorkDescription(entity.getAiWorkDescription());

        // JSON 문자열 -> 리스트 복원
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setMaterials(convertJsonStringToMaterialList(entity.getMaterials()));

        return dto;
    }

    // =================================================================================
    //                            JSON 변환 유틸리티 메서드
    // =================================================================================

    // List 객체 -> JSON String
    private String convertListToJsonString(Object list) {
        if (list == null) return null;
        try { return objectMapper.writeValueAsString(list); }
        catch (JsonProcessingException e) { throw new RuntimeException("JSON 변환 오류", e); }
    }

    // JSON String -> List<String>
    private List<String> convertJsonStringToListString(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); }
        catch (JsonProcessingException e) { throw new RuntimeException("JSON 파싱 오류", e); }
    }

    // JSON String -> List<EquipmentDto>
    private List<EquipmentDto> convertJsonStringToEquipmentList(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<EquipmentDto>>() {}); }
        catch (JsonProcessingException e) { throw new RuntimeException("JSON 파싱 오류", e); }
    }

    // JSON String -> List<MaterialDto>
    private List<MaterialDto> convertJsonStringToMaterialList(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<MaterialDto>>() {}); }
        catch (JsonProcessingException e) { throw new RuntimeException("JSON 파싱 오류", e); }
    }
}