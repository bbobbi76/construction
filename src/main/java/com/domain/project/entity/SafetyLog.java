package com.domain.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity // 이 클래스가 DB 테이블임을 알림
public class SafetyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 일지 고유 ID

    private String company;
    private String logDate;
    private String weather;
    private String location;

    @Lob
    private String workDetails;

    private String workType;
    private int workersCount;

    @Lob
    private String workerNames; // DTO의 List<String> -> JSON 문자열로 변환하여 저장

    @Lob
    private String remarks;

    private String manager;

    @Lob
    private String equipment; // DTO의 List<EquipmentDto> -> JSON 문자열로 변환하여 저장

    @Lob
    private String photos; // DTO의 List<String> -> JSON 문자열로 변환하여 저장

    @Lob
    private String signature; // DTO의 String (Base64 또는 URL) -> 긴 텍스트로 저장

    @Lob
    private String attachments; // DTO의 List<String> -> JSON 문자열로 변환하여 저장

    // --- 안전일지 고유 항목 ---
    @Lob
    private String riskFactors;

    @Lob
    private String correctiveActions;

    @Lob
    private String safetyChecklist; // DTO의 List<SafetyCheckItemDto> -> JSON 문자열로 변환


    // --- Getter/Setter (JPA가 사용) ---

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

    public String getWorkerNames() {
        return workerNames;
    }
    public void setWorkerNames(String workerNames) {
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

    public String getEquipment() {
        return equipment;
    }
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getPhotos() {
        return photos;
    }
    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAttachments() {
        return attachments;
    }
    public void setAttachments(String attachments) {
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

    public String getSafetyChecklist() {
        return safetyChecklist;
    }
    public void setSafetyChecklist(String safetyChecklist) {
        this.safetyChecklist = safetyChecklist;
    }
}