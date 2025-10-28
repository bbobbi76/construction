package com.domain.project.dto;

import java.util.List;

// EquipmentDto 클래스는 ConstructionLogDto.java 파일에 이미 정의되어 있으므로
// 같은 패키지(com.domain.project.dto)에서 바로 사용할 수 있습니다.

/**
 * 안전일지 (SafetyLog) JSON 구조와 1:1로 매핑되는 DTO(Data Transfer Object) 클래스.
 * 웹(JavaScript)에서 이 구조로 JSON을 보내면, Spring Boot가 이 Java 객체로 자동 변환해줍니다.
 */
public class SafetyLogDto {

    // --- 공통 항목 ---

    /**
     * 시행업체 이름 (예: "OO 건설")
     */
    private String company;

    /**
     * 일지 작성 날짜 (예: "2025-10-28")
     */
    private String logDate;

    /**
     * 날씨 (예: "맑음", "흐림")
     */
    private String weather;

    /**
     * 작업 위치 (예: "A동 101호", "지하 2층")
     */
    private String location;

    /**
     * 작업 내용 (여러 줄 입력 가능. 예: "1. TBM 실시\n2. 안전 설비 점검")
     */
    private String workDetails;

    /**
     * 작업 인원 수 (숫자. 예: 5)
     */
    private int workersCount;

    /**
     * 실제 작업자 이름 목록 (배열). 예: ["김철수", "이영희"]
     */
    private List<String> workerNames;

    /**
     * 특이사항 (여러 줄 입력 가능. 예: "오전 9시 TBM 실시 완료")
     */
    private String remarks;

    /**
     * 안전 관리자 이름 (예: "박안전")
     */
    private String manager;

    /**
     * 투입된 장비 목록 (배열). EquipmentDto 객체의 리스트입니다.
     * (EquipmentDto는 ConstructionLogDto.java 파일에 정의되어 있습니다)
     */
    private List<EquipmentDto> equipment;

    /**
     * 현장 사진 파일의 경로(URL) 목록 (배열).
     * 예: ["/uploads/safety_img1.jpg"]
     */
    private List<String> photos;

    /**
     * 관리자 서명 이미지의 경로(URL).
     * 예: "/uploads/sign_safety.png"
     */
    private String signature;

    /**
     * 첨부 서류 파일의 경로(URL) 목록 (배열).
     * 예: ["/docs/safety_checklist.pdf"]
     */
    private List<String> attachments;

    // --- 안전일지 고유 항목 ---

    /**
     * 위험 요소 (여러 줄 입력 가능. 예: "1. 추락 위험\n2. 환기 불량")
     */
    private String riskFactors;

    /**
     * 개선 대책 (여러 줄 입력 가능. 예: "1. 안전망 설치\n2. 환풍기 가동")
     */
    private String correctiveActions;


    // --- Getter 및 Setter ---
    // Spring Boot가 JSON 데이터를 이 객체에 주입(Injection)하거나
    // 이 객체를 JSON으로 변환할 때 자동으로 이 메서드들을 호출합니다.

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
}