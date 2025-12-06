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

/**
 * [안전일지 서비스]
 * 안전일지 관련 비즈니스 로직을 처리하는 계층입니다.
 * - Controller와 Repository 사이에서 데이터를 가공(DTO <-> Entity 변환)하고,
 * - 리스트 데이터(장비, 체크리스트 등)를 JSON 문자열로 변환하여 DB에 저장합니다.
 */
@Service
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화를 위해 읽기 전용 모드 사용
public class SafetyLogService {

    private final SafetyLogRepository safetyLogRepository;
    private final ObjectMapper objectMapper; // JSON 변환기 (List 객체 <-> String)

    // 생성자 주입 (Dependency Injection)
    public SafetyLogService(SafetyLogRepository safetyLogRepository, ObjectMapper objectMapper) {
        this.safetyLogRepository = safetyLogRepository;
        this.objectMapper = objectMapper;
    }

    // =================================================================================
    //                                  CRUD 기능
    // =================================================================================

    /**
     * 1. 안전일지 생성 (저장)
     * - DTO를 Entity로 변환 후 DB에 저장합니다.
     * - 작성자(author) 정보를 주입하여 누가 썼는지 기록합니다.
     */
    @Transactional // 쓰기 작업이므로 readOnly = false
    public SafetyLogDto createLog(SafetyLogDto dto, String username) {
        // 1. DTO -> Entity 변환
        SafetyLog entity = dtoToEntity(dto);

        // 2. 작성자 정보 주입
        entity.setAuthor(username);

        // 3. DB 저장
        SafetyLog savedEntity = safetyLogRepository.save(entity);

        // 4. 저장된 Entity를 다시 DTO로 변환하여 반환
        return entityToDto(savedEntity);
    }

    /**
     * 2. 안전일지 상세 조회
     * - ID로 일지를 찾아 반환하며, 없으면 예외를 발생시킵니다.
     */
    public SafetyLogDto getLogById(Long id) {
        SafetyLog entity = safetyLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id));
        return entityToDto(entity);
    }

    /**
     * 3. 내 안전일지 목록 조회
     * - 로그인한 사용자(username)가 작성한 글만 최신순으로 가져옵니다.
     */
    public List<SafetyLogDto> findAllMyLogs(String username) {
        List<SafetyLog> entities = safetyLogRepository.findByAuthorOrderByLogDateDesc(username);

        // Stream을 사용하여 Entity 리스트를 DTO 리스트로 일괄 변환
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * 4. 안전일지 수정
     * - Dirty Checking(변경 감지)을 통해 데이터를 업데이트합니다.
     */
    @Transactional
    public SafetyLogDto updateLog(Long id, SafetyLogDto dto) {
        // 1. 기존 데이터 조회
        SafetyLog entity = safetyLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id));

        // 2. 데이터 업데이트 (DTO의 내용으로 Entity 필드 갱신)
        updateEntityFromDto(entity, dto);

        // 3. 저장 (사실 Transaction 종료 시 자동 커밋되지만, 명시적으로 호출)
        SafetyLog updatedEntity = safetyLogRepository.save(entity);
        return entityToDto(updatedEntity);
    }

    /**
     * 5. 안전일지 삭제
     */
    @Transactional
    public void deleteLog(Long id) {
        if (!safetyLogRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 안전일지가 없습니다. id=" + id);
        }
        safetyLogRepository.deleteById(id);
    }

    /**
     * 6. 가장 최근 일지 조회 (전일 데이터 불러오기용)
     * - 작성 화면 진입 시, 이전 데이터를 자동으로 채워주기 위해 사용합니다.
     */
    public SafetyLogDto getLastLog(String username) {
        SafetyLog entity = safetyLogRepository.findTopByAuthorOrderByLogDateDesc(username)
                .orElseThrow(() -> new IllegalArgumentException("이전 일지가 없습니다."));
        return entityToDto(entity);
    }

    // =================================================================================
    //                            Entity <-> DTO 변환 로직
    // =================================================================================

    /**
     * DTO의 데이터를 Entity에 업데이트
     * - 공사일지와 달리 '위험성 평가', '체크리스트', '조치사항' 등의 추가 필드가 있습니다.
     * - List 필드들은 JSON 문자열로 변환하여 저장합니다.
     */
    private void updateEntityFromDto(SafetyLog entity, SafetyLogDto dto) {
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

        // 안전일지 고유 필드 (AI 분석 결과 등)
        entity.setPotentialRiskFactors(dto.getPotentialRiskFactors());
        entity.setCountermeasures(dto.getCountermeasures());
        entity.setMajorRiskFactors(dto.getMajorRiskFactors());
        entity.setFollowUpPhoto(dto.getFollowUpPhoto());
        entity.setCorrectiveActions(dto.getCorrectiveActions());

        // 리스트 -> JSON 문자열 변환
        entity.setWorkerNames(convertListToJsonString(dto.getWorkerNames()));
        entity.setPhotos(convertListToJsonString(dto.getPhotos()));
        entity.setAttachments(convertListToJsonString(dto.getAttachments()));
        entity.setEquipment(convertListToJsonString(dto.getEquipment()));
        entity.setSafetyChecklist(convertListToJsonString(dto.getSafetyChecklist()));
    }

    /**
     * DTO -> Entity 변환
     */
    private SafetyLog dtoToEntity(SafetyLogDto dto) {
        SafetyLog entity = new SafetyLog();
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * Entity -> DTO 변환
     * - DB에 저장된 JSON 문자열을 다시 List 객체로 복원합니다.
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

        // 안전일지 고유 필드 복원
        dto.setPotentialRiskFactors(entity.getPotentialRiskFactors());
        dto.setCountermeasures(entity.getCountermeasures());
        dto.setMajorRiskFactors(entity.getMajorRiskFactors());
        dto.setFollowUpPhoto(entity.getFollowUpPhoto());
        dto.setCorrectiveActions(entity.getCorrectiveActions());

        // JSON 문자열 -> 리스트 복원
        dto.setWorkerNames(convertJsonStringToListString(entity.getWorkerNames()));
        dto.setPhotos(convertJsonStringToListString(entity.getPhotos()));
        dto.setAttachments(convertJsonStringToListString(entity.getAttachments()));
        dto.setEquipment(convertJsonStringToEquipmentList(entity.getEquipment()));
        dto.setSafetyChecklist(convertJsonStringToSafetyCheckList(entity.getSafetyChecklist()));

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

    // JSON String -> List<SafetyCheckItemDto> (안전일지 전용)
    private List<SafetyCheckItemDto> convertJsonStringToSafetyCheckList(String json) {
        if (json == null || json.isEmpty()) return null;
        try { return objectMapper.readValue(json, new TypeReference<List<SafetyCheckItemDto>>() {}); }
        catch (JsonProcessingException e) { throw new RuntimeException("JSON 파싱 오류", e); }
    }
}