package com.domain.project.entity;

// jakarta.persistence는 JPA(DB 연동 기술) 관련 라이브러리입니다.
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob; // Lob 어노테이션 임포트

@Entity // 이 클래스가 DB 테이블임을 JPA에게 알림
public class ConstructionLog {

    @Id // 이 필드가 Primary Key(고유 식별자)임을 알림
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 id를 자동으로 생성/증가 (Auto-increment)
    private Long id; // 일지 고유 ID (숫자 타입)

    private String company;
    private String logDate;
    private String weather;
    private String location;

    @Lob // Large Object: 아주 긴 텍스트(여러 줄)를 저장할 수 있음
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

    private String author; //작성자

    // --- 공사일지 고유 항목 ---
    @Lob
    private String materials; // DTO의 List<MaterialDto> -> JSON 문자열로 변환하여 저장


    // --- Getter/Setter ---
    // JPA가 DB에서 데이터를 조회/저장할 때 사용합니다.

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

    public String getMaterials() {
        return materials;
    }
    public void setMaterials(String materials) {
        this.materials = materials;
    }
    // (작성자) author의 Getter/Setter
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
}