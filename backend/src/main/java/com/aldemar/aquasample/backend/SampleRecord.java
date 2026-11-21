package com.aldemar.aquasample.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "sample_record", uniqueConstraints = @UniqueConstraint(name = "uq_sample_code", columnNames = "code"))
public class SampleRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 40)
    private String code;
    private String concession;
    @Column(nullable = false) private String center;
    @Column(nullable = false) private String train;
    @Column(nullable = false, name = "line_name") private String line;
    @Column(nullable = false) private int sectionLengthCm;
    @Column(nullable = false) private long timestamp;
    @Column(nullable = false) private String operatorName;
    @Column(nullable = false) private int manualCount;
    @Column(columnDefinition = "TEXT") private String markerCoordinatesJson = "[]";
    @Column(columnDefinition = "TEXT") private String observations;
    private String photoPath;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25)
    private SampleStatus reviewStatus = SampleStatus.PENDIENTE_REVISION;
    @Column(length = 500) private String supervisorComment;
    private String supervisorName;
    @Column(nullable = false) private long updatedAt;

    @PrePersist @PreUpdate
    void touch() { updatedAt = System.currentTimeMillis(); }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
    public String getConcession() { return concession; }
    public void setConcession(String v) { concession = v; }
    public String getCenter() { return center; }
    public void setCenter(String v) { center = v; }
    public String getTrain() { return train; }
    public void setTrain(String v) { train = v; }
    public String getLine() { return line; }
    public void setLine(String v) { line = v; }
    public int getSectionLengthCm() { return sectionLengthCm; }
    public void setSectionLengthCm(int v) { sectionLengthCm = v; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String v) { operatorName = v; }
    public int getManualCount() { return manualCount; }
    public void setManualCount(int v) { manualCount = v; }
    public String getMarkerCoordinatesJson() { return markerCoordinatesJson; }
    public void setMarkerCoordinatesJson(String v) { markerCoordinatesJson = v; }
    public String getObservations() { return observations; }
    public void setObservations(String v) { observations = v; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String v) { photoPath = v; }
    public SampleStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(SampleStatus v) { reviewStatus = v; }
    public String getSupervisorComment() { return supervisorComment; }
    public void setSupervisorComment(String v) { supervisorComment = v; }
    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String v) { supervisorName = v; }
    public long getUpdatedAt() { return updatedAt; }
}
