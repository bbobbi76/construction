package com.domain.project.dto;

import java.util.List;

/**
 * 안전일지 (SafetyLog) 메인 DTO 클래스.
 * 이제부터 별도의 public 파일로 분리된 EquipmentDto와 SafetyCheckItemDto를 참조합니다.
 */
public class SafetyLogDto {

    // --- 공통 항목 ---
    private Long id; // DB에서 생성된 고유 ID
    private String company;
    private String logDate;
    private String weather;
    private String location;
    private String workDetails;
    private String workType;
    private int workersCount;
    private List<String> workerNames;
    private String remarks;
    private String manager;
    private List<EquipmentDto> equipment; // (별도 파일의 public EquipmentDto를 참조)
    private List<String> photos;
    private String signature;
    private List<String> attachments;

    // --- 안전일지 고유 항목 ---
    private String riskFactors;
    private String correctiveActions;
    private List<SafetyCheckItemDto> safetyChecklist; // (별도 파일의 public SafetyCheckItemDto를 참조)


    // --- Getter 및 Setter (전체) ---

    // id의 Getter/Setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getLogDate() {
        return logDate;
    }
    public void setLogDate(String logDate) {
        this.logDate = logDate;
    }

    public String getWeather() {
        return weather;
    }
    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getWorkDetails() {
        return workDetails;
    }
    public void setWorkDetails(String workDetails) {
        this.workDetails = workDetails;
    }

    public String getWorkType() {
        return workType;
    }
    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public int getWorkersCount() {
        return workersCount;
    }
    public void setWorkersCount(int workersCount) {
        this.workersCount = workersCount;
    }

    public List<String> getWorkerNames() {
        return workerNames;
    }
    public void setWorkerNames(List<String> workerNames) {
        this.workerNames = workerNames;
    }

    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getManager() {
        return manager;
    }
    public void setManager(String manager) {
        this.manager = manager;
    }

    public List<EquipmentDto> getEquipment() {
        return equipment;
    }
    public void setEquipment(List<EquipmentDto> equipment) {
        this.equipment = equipment;
    }

    public List<String> getPhotos() {
        return photos;
    }
    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getAttachments() {
        return attachments;
    }
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getRiskFactors() {
        return riskFactors;
    }
    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getCorrectiveActions() {
        return correctiveActions;
    }
    public void setCorrectiveActions(String correctiveActions) {
        this.correctiveActions = correctiveActions;
    }

    public List<SafetyCheckItemDto> getSafetyChecklist() {
        return safetyChecklist;
    }
    public void setSafetyChecklist(List<SafetyCheckItemDto> safetyChecklist) {
        this.safetyChecklist = safetyChecklist;
    }
}