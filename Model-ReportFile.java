package com.scb.rwtoolbackend.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_files")
public class ReportFile {
    
    // Statuses reflecting file lifecycle stages
    public enum TransferStatus {
        NEW, READY_TO_TRANSFER, PROCESSING, TRANSFERRED, FAILED
    }

    public enum StorageLocation {
        REMOTE, LOCAL
    }

    @Id
    private String fileId;
    private String fileName;
    private String remotePath;
    private String localPath;
    private String destinationGroup;
    
    @Enumerated(EnumType.STRING)
    private TransferStatus transferStatus;
    
    @Enumerated(EnumType.STRING)
    private StorageLocation currentLocation;
    
    private LocalDateTime dateCreated;

    // --- Constructors ---
    public ReportFile() {}

    public ReportFile(String fileId, String fileName, String remotePath, String localPath, 
                      String destinationGroup, TransferStatus transferStatus, 
                      StorageLocation currentLocation, LocalDateTime dateCreated) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.remotePath = remotePath;
        this.localPath = localPath;
        this.destinationGroup = destinationGroup;
        this.transferStatus = transferStatus;
        this.currentLocation = currentLocation;
        this.dateCreated = dateCreated;
    }

    // --- Getters and Setters (Required for JPA/Lombok equivalent) ---
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getRemotePath() { return remotePath; }
    public void setRemotePath(String remotePath) { this.remotePath = remotePath; }
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public String getDestinationGroup() { return destinationGroup; }
    public void setDestinationGroup(String destinationGroup) { this.destinationGroup = destinationGroup; }
    public TransferStatus getTransferStatus() { return transferStatus; }
    public void setTransferStatus(TransferStatus transferStatus) { this.transferStatus = transferStatus; }
    public StorageLocation getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(StorageLocation currentLocation) { this.currentLocation = currentLocation; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }
}
