package com.domain.project.dto;

import java.util.List;

/**
 * JSON의 equipment 항목({ "name": "...", "count": ... })을 위한 DTO 클래스.
 * ConstructionLogDto 내에서 사용됩니다.
 */
class EquipmentDto {
    /**
     * 장비 이름 (예: "굴삭기")
     */
    private String name;
    /**
     * 장비 수량 (숫자. 예: 1)
     */
    private int count;

    // --- Getter/Setter ---
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}

/**
 * JSON의 materials 항목({ "name": "...", "quantity": ... })을 위한 DTO 클래스.
 * ConstructionLogDto 내에서 사용됩니다.
 */
class MaterialDto {
    /**
     * 자재 이름 (예: "시멘트")
     */
    private String name;
    /**
     * 자재 수량 (단위 포함 텍스트. 예: "10포", "100장")
     */
    private String quantity;

    // --- Getter/Setter ---
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getQuantity() {
        return quantity;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}


/**
 * 공사일지 (ConstructionLog) JSON 구조와 1:1로 매핑되는 DTO(Data Transfer Object) 클래스.
 * 웹(JavaScript)에서 이 구조로 JSON을 보내면, Spring Boot가 이 Java 객체로 자동 변환해줍니다.
 */
public class ConstructionLogDto {

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
     * 작업 위치 (예: "A동 101호")
     */
    private String location;

    /**
     * 작업 내용 (여러 줄 입력 가능. 예: "1. 내부 도배\n2. 자재 정리")
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
     * 특이사항 (여러 줄 입력 가능. 예: "오후 3시 자재 추가 반입")
     */
    private String remarks;

    /**
     * 현장 관리자 이름 (예: "최반장")
     */
    private String manager;

    /**
     * 투입된 장비 목록 (배열). EquipmentDto 객체의 리스트입니다.
     */
    private List<EquipmentDto> equipment;

    /**
     * 현장 사진 파일의 경로(URL) 목록 (배열).
     * 예: ["/uploads/img1.jpg", "/uploads/img2.jpg"]
     */
    private List<String> photos;

    /**
     * 관리자 서명 이미지의 경로(URL).
     * 예: "/uploads/sign_manager.png"
     */
    private String signature;

    /**
     * 첨부 서류 파일의 경로(URL) 목록 (배열).
     * 예: ["/docs/work_permit.pdf"]
     */
    private List<String> attachments;

    // --- 공사일지 고유 항목 ---

    /**
     * 투입된 자재 목록 (배열). MaterialDto 객체의 리스트입니다.
     */
    private List<MaterialDto> materials;


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

    public List<MaterialDto> getMaterials() {
        return materials;
    }
    public void setMaterials(List<MaterialDto> materials) {
        this.materials = materials;
    }
}